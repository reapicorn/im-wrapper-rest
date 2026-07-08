/*
 * Copyright (C) 2025  reapicorn
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package io.github.reapicorn.im.internal;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal JSON parser sufficient for the IM REST API responses.
 *
 * <p>Supports only the subset of JSON actually returned by the API:
 * objects, arrays, strings, numbers, booleans, and null. No external
 * library dependency is required — this is intentionally lightweight.
 *
 * <p>The parser is <em>not</em> a general-purpose JSON library; it uses
 * a recursive-descent approach focused on correctness for the known
 * response shapes.
 */
public final class JsonParser {

    private final String src;
    private int pos;

    private JsonParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    // ------------------------------------------------------------------
    //  Public entry points
    // ------------------------------------------------------------------

    /** Parses the JSON text and returns Object / List / Map / String / Number / Boolean / null. */
    public static Object parse(String json) {
        if (json == null || json.isBlank()) return null;
        return new JsonParser(json.strip()).parseValue();
    }

    /** Convenience: parse and cast to {@code Map<String,Object>}. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object result = parse(json);
        if (result instanceof Map) return (Map<String, Object>) result;
        return Collections.emptyMap();
    }

    /** Convenience: parse and cast to {@code List<Object>}. */
    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object result = parse(json);
        if (result instanceof List) return (List<Object>) result;
        if (result instanceof Map) return List.of(result);
        return Collections.emptyList();
    }

    // ------------------------------------------------------------------
    //  Recursive descent
    // ------------------------------------------------------------------

    private Object parseValue() {
        skipWs();
        if (pos >= src.length()) return null;
        char c = src.charAt(pos);
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'f' -> parseBool();
            case 'n' -> parseNull();
            default  -> parseNumber();
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseObject() {
        consume('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (peek() == '}') { consume('}'); return map; }
        while (true) {
            String key = parseString();
            skipWs(); consume(':'); skipWs();
            Object val = parseValue();
            map.put(key, val);
            skipWs();
            if (peek() == ',') { consume(','); skipWs(); } else break;
        }
        consume('}');
        return map;
    }

    private List<Object> parseArray() {
        consume('[');
        List<Object> list = new ArrayList<>();
        skipWs();
        if (peek() == ']') { consume(']'); return list; }
        while (true) {
            list.add(parseValue());
            skipWs();
            if (peek() == ',') { consume(','); skipWs(); } else break;
        }
        consume(']');
        return list;
    }

    private String parseString() {
        consume('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\') {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case 'u'  -> {
                        String hex = src.substring(pos, pos + 4); pos += 4;
                        sb.append((char) Integer.parseInt(hex, 16));
                    }
                    default   -> sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Boolean parseBool() {
        if (src.startsWith("true", pos))  { pos += 4; return Boolean.TRUE;  }
        if (src.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        throw new IllegalStateException("Unexpected token at pos " + pos + ": " + src.substring(pos, Math.min(pos + 10, src.length())));
    }

    private Object parseNull() {
        if (src.startsWith("null", pos)) { pos += 4; return null; }
        throw new IllegalStateException("Unexpected token at pos " + pos);
    }

    private Number parseNumber() {
        int start = pos;
        if (pos < src.length() && src.charAt(pos) == '-') pos++;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
            pos++;
        }
        String numStr = src.substring(start, pos);
        if (numStr.isEmpty() || numStr.equals("-")) {
            throw new IllegalStateException("Unexpected character at pos " + start +
                    ": '" + (start < src.length() ? src.charAt(start) : "EOF") + "'");
        }
        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
            return Double.parseDouble(numStr);
        }
        try { return Long.parseLong(numStr); }
        catch (NumberFormatException e) { return Double.parseDouble(numStr); }
    }

    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    private char peek() {
        skipWs();
        return pos < src.length() ? src.charAt(pos) : 0;
    }

    private void consume(char expected) {
        skipWs();
        if (pos >= src.length() || src.charAt(pos) != expected) {
            throw new IllegalStateException("Expected '" + expected + "' at pos " + pos +
                    " but found: " + (pos < src.length() ? src.charAt(pos) : "EOF"));
        }
        pos++;
    }

    // ------------------------------------------------------------------
    //  Serialisation helpers
    // ------------------------------------------------------------------

    /** Serialises a Map to a JSON object string. */
    public static String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(escape(e.getKey())).append("\":").append(valueToJson(e.getValue()));
            first = false;
        }
        return sb.append('}').toString();
    }

    /** Serialises a List to a JSON array string. */
    public static String toJson(List<?> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(',');
            sb.append(valueToJson(item));
            first = false;
        }
        return sb.append(']').toString();
    }

    @SuppressWarnings("unchecked")
    private static String valueToJson(Object v) {
        if (v == null)             return "null";
        if (v instanceof String)   return '"' + escape((String) v) + '"';
        if (v instanceof Boolean)  return v.toString();
        if (v instanceof Number)   return v.toString();
        if (v instanceof Map)      return toJson((Map<String, Object>) v);
        if (v instanceof List)     return toJson((List<?>) v);
        return '"' + escape(v.toString()) + '"';
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
