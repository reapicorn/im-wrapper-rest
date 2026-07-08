# im-wrapper-rest

A pure Java library that wraps the **IBM Identity Manager (IM)** REST API.
Also known as **IBM Tivoli Identity Manager (ITIM)**, **IBM Security Identity Manager (ISIM)**, **IBM Security Verify Governance - Identity Manager (ISVG IM)** and **IBM Verify Identity Governance (IVIG)**.
Operate on people, accounts, roles, policies, certification campaigns, ARC risks,
and every other IM domain from any Java application — no external frameworks required.

API reference documentation: [IM - REST API](https://www.ibm.com/docs/en/sig-and-i/11.0.1?topic=reference-rest-api)

---

## Requirements

| Requirement | Minimum version |
|-------------|----------------|
| JDK         | IBM Semeru / OpenJDK **25** |
| OS          | Windows / Linux / macOS |
| IM        | Any version with the REST API enabled |

No external dependencies. JSON is parsed internally and the transport layer uses
`java.net.http.HttpClient` from the JDK.

---

## Package structure

```
io.github.reapicorn.im/
├── IMClient.java          ← main entry point
├── IMConfig.java          ← configuration (URL, credentials, SSL, timeouts)
├── Result.java              ← return type for every operation
├── IMException.java       ← error wrapped inside Result.failure
├── SearchParams.java        ← query parameters (attributes, limit, sort, embedded)
├── PageRange.java           ← Range header for pagination
├── model/                   ← response models
│   ├── Person.java
│   ├── Account.java
│   ├── Role.java
│   ├── SystemUser.java
│   ├── Service.java
│   ├── ServiceProfile.java
│   ├── OrgContainer.java
│   ├── RequestResponse.java ← async 202 response
│   └── HalResource.java     ← generic HAL resource (links + attributes + embedded)
├── service/                 ← one service class per REST domain
│   ├── PeopleService.java
│   ├── AccountsService.java
│   ├── RolesService.java
│   ├── ... (see service reference table)
│   ├── cm/                  ← subsystem /itim/cm — Certification Campaigns
│   │   ├── CertificationCampaignsModule.java
│   │   ├── CampaignsService.java
│   │   ├── CampaignInstancesService.java
│   │   ├── CampaignAssignmentsService.java
│   │   └── CampaignRecommendationsService.java
│   └── arc/                 ← subsystem /itim/arc — ARC / Risk Management
│       ├── ArcModule.java
│       ├── ArcActivitiesService.java
│       ├── ArcRisksService.java
│       ├── ArcMitigationsService.java
│       ├── ArcUsersService.java
│       └── ArcStatsService.java
└── internal/                ← do not use directly
    ├── HttpExecutor.java
    ├── JsonParser.java
    ├── HalParser.java
    └── TrustAllSslContext.java
```

---

## Build

```bash
mvn package
```

Produces `target/im-wrapper-rest-1.0.0.jar`. Add it to your project's classpath.

---

## Examples

Runnable examples are in the [`examples/`](examples/) folder:

| File | What it shows |
|------|---------------|
| [`GettingStartedExample.java`](examples/GettingStartedExample.java) | Configure client, start session, inspect config |
| [`SystemUserExample.java`](examples/SystemUserExample.java) | `GET /systemusers/me` — authenticated system account |
| [`PersonExample.java`](examples/PersonExample.java) | `GET /people/me` — authenticated person record |
| [`ErrorHandlingExample.java`](examples/ErrorHandlingExample.java) | How `Result<T>` and `IMException` look on failure |

Each file is a self-contained JDK 25 unnamed class. Run with:

```bash
javac --release 25 -cp target/im-wrapper-rest-1.0.0.jar examples/GettingStartedExample.java
java  -cp "target/im-wrapper-rest-1.0.0.jar:examples" GettingStartedExample
```

---

## Basic usage

### 1. Configure and create the client

```java
IMConfig config = IMConfig.builder()
    .baseUrl("https://your-im-host:30943/itim/rest")
    .username("itim manager")
    .password("your-password")
    .trustAllSsl(true)          // dev/test with self-signed certificate
    .build();

IMClient client = new IMClient(config);
```

### 2. Start a session

`initSession()` posts credentials to `j_security_check` to obtain the LTPA session
cookie, then calls `GET /systemusers/me` to capture the CSRF token required for write
operations. Call it once before using any service.

```java
Result<Void> session = client.initSession();
if (!session.isSuccess()) {
    System.err.println("Session init failed: " + session.getError());
    return;
}
```

### 3. Use a service

Services are accessed through lazy-initialised getters on the client. Instances are
created on first call and cached — safe to call the getter multiple times.

```java
SearchParams params = SearchParams.builder()
    .attributes("cn", "sn", "mail")
    .limit(50)
    .sort("+cn")
    .build();

Result<List<Person>> result = client.people().searchPeople(params, null);

if (result.isSuccess()) {
    for (Person p : result.getValue()) {
        System.out.println(p.getName() + " — " + p.getAttribute("mail"));
    }
} else {
    System.err.println("Error: " + result.getError().getMessage());
    System.err.println("HTTP status: " + result.getError().getStatusCode());
    System.err.println("Body: " + result.getError().getResponseBody());
}
```

---

## `Result<T>` handling

Every operation returns `Result<T>`. **No business exceptions are thrown** to the caller.

```java
result.isSuccess()      // true if the call succeeded
result.getValue()       // unwrap the value (throws IllegalStateException if failure)
result.getError()       // unwrap the IMException (throws IllegalStateException if success)

// Transform without unwrapping
Result<String> name = result.map(persons -> persons.get(0).getName());

// Chain operations that also return Result
Result<Person> person = result.flatMap(persons ->
    client.people().getPerson(persons.get(0).getId(), null));
```

`IMException` exposes:
- `getMessage()` — human-readable error description
- `getStatusCode()` — HTTP status code (0 for network-level errors)
- `getResponseBody()` — raw response body, or `null` if not available

---

## SSL

| Option               | When to use                                                        |
|----------------------|--------------------------------------------------------------------|
| `trustAllSsl(true)`  | Dev / test with self-signed certificates                           |
| `sslContext(ctx)`    | Production: inject an `SSLContext` built with your own CA certificate |

If both are set, `sslContext` takes precedence.

```java
// Load the server certificate into a KeyStore
KeyStore ks = KeyStore.getInstance("JKS");
try (InputStream is = new FileInputStream("im-cert.jks")) {
    ks.load(is, "changeit".toCharArray());
}

TrustManagerFactory tmf = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
tmf.init(ks);

SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(null, tmf.getTrustManagers(), null);

IMConfig config = IMConfig.builder()
    .baseUrl("https://im-prod.company.com/itim/rest")
    .username("svc-integration")
    .password(password)
    .sslContext(sslContext)
    .build();
```

Export the certificate from the server:

```bash
openssl s_client -connect im-prod.company.com:30943 -showcerts </dev/null 2>/dev/null \
  | openssl x509 -outform PEM > im.pem

keytool -importcert -alias im -file im.pem \
        -keystore im-cert.jks -storepass changeit -noprompt
```

---

## Example: create a person and poll the async request

Write operations that IM processes asynchronously return HTTP 202 with a
`RequestResponse` containing the `requestId`. The caller controls when and how long
to poll.

```java
Map<String, List<String>> attrs = new LinkedHashMap<>();
attrs.put("cn",        List.of("Jane Doe"));
attrs.put("sn",        List.of("Doe"));
attrs.put("givenname", List.of("Jane"));
attrs.put("mail",      List.of("jdoe@company.com"));

String orgId = "erglobalid=00000000000000000000,ou=org,dc=company,dc=com";

Result<RequestResponse> createResult =
        client.people().createPerson(orgId, "Person", attrs);

if (!createResult.isSuccess()) {
    System.err.println("Create failed: " + createResult.getError());
    return;
}

String requestId = createResult.getValue().getRequestId();
System.out.println("Request submitted: " + requestId);

// Poll until complete (max 30 attempts, 2 s apart)
for (int i = 0; i < 30; i++) {
    Thread.sleep(2000);
    Result<HalResource> poll = client.requests().getRequest(requestId);
    if (poll.isSuccess()) {
        String changeComplete = poll.getValue().getAttribute("changeComplete");
        if ("true".equalsIgnoreCase(changeComplete)) {
            System.out.println("Done: " + poll.getValue());
            break;
        }
    }
}
```

---

## Service reference

| `IMClient` method             | Service class                    | REST domain                                    |
|---------------------------------|----------------------------------|------------------------------------------------|
| `people()`                      | `PeopleService`                  | `/people`                                      |
| `accounts()`                    | `AccountsService`                | `/accounts`                                    |
| `roles()`                       | `RolesService`                   | `/roles`                                       |
| `systemUsers()`                 | `SystemUsersService`             | `/systemusers`                                 |
| `organizations()`               | `OrganizationsService`           | `/organizationcontainers`                      |
| `services()`                    | `ServicesService`                | `/services`, `/serviceprofiles`                |
| `access()`                      | `AccessService`                  | `/access`                                      |
| `accessCategories()`            | `AccessCategoriesService`        | `/accesscategories`                            |
| `requests()`                    | `RequestsService`                | `/requests`                                    |
| `activities()`                  | `ActivitiesService`              | `/activities`                                  |
| `workitems()`                   | `WorkitemsService`               | `/workitems`                                   |
| `workflows()`                   | `WorkflowsService`               | `/workflows`                                   |
| `passwordManagement()`          | `PasswordManagementService`      | `/password`                                    |
| `passwordPolicies()`            | `PasswordPolicyService`          | `/passwordpolicy`                              |
| `forms()`                       | `FormsService`                   | `/forms`, `/formtemplates`                     |
| `views()`                       | `ViewsService`                   | `/views`                                       |
| `entities()`                    | `EntitiesService`                | `/entities`                                    |
| `groups()`                      | `GroupsService`                  | `/groups` (v1.2 only)                          |
| `systemRoles()`                 | `SystemRolesService`             | `/systemroles`                                 |
| `identityPolicies()`            | `IdentityPolicyService`          | `/identitypolicy`                              |
| `provisioningPolicies()`        | `ProvisioningPoliciesService`    | `/provisioningpolicies`                        |
| `adoptionPolicies()`            | `AdoptionPoliciesService`        | `/adoptionpolicies`                            |
| `lifecycleRules()`              | `LifecycleRuleService`           | `/lifecyclerule`                               |
| `acis()`                        | `AciService`                     | `/acis`                                        |
| `entitlements()`                | `EntitlementsService`            | `/entitlements/assignments`                    |
| `authz()`                       | `AuthzService`                   | `/authz/v1.0/`                                 |
| `apiSecurity()`                 | `ApiSecurityService`             | `/apisecurity/v1.0/`                           |
| `attributes()`                  | `AttributesService`              | `/attributes/`                                 |
| `certificationCampaigns()`      | `CertificationCampaignsModule`   | `/itim/cm/v2.0/` (module with 4 sub-services)  |
| `arc()`                         | `ArcModule`                      | `/itim/arc/v1.0/` (module with 5 sub-services) |

### `certificationCampaigns()` module

```java
client.certificationCampaigns().campaigns()        // campaign configurations
client.certificationCampaigns().instances()        // running campaign instances
client.certificationCampaigns().assignments()      // reviewer assignments
client.certificationCampaigns().recommendations()  // recommendations and insights
```

### `arc()` module

```java
client.arc().activities()   // business activities and folders
client.arc().risks()        // risk management
client.arc().mitigations()  // mitigation controls
client.arc().users()        // per-user risk analysis
client.arc().stats()        // statistics, entity search, bulk operations
```

---

## Notes

- **Session**: `initSession()` posts to `j_security_check` (form-based WebSphere auth)
  to obtain the LTPA session cookie, then fetches `GET /systemusers/me` to capture the
  CSRF token. Both steps are automatic — no configuration needed beyond username/password.
- **CSRF**: the CSRF token is injected automatically into every `POST`, `PUT`, and
  `DELETE` request. No manual handling required.
- **Async operations**: when IM responds HTTP 202, the `requestId` is available via
  `RequestResponse.getRequestId()`. Use `client.requests().getRequest(id)` to poll.
  The library does not poll automatically — the caller controls the pace and timeout.
- **Timeouts**: configurable in `IMConfig` via `connectTimeoutSeconds` (default 30 s)
  and `readTimeoutSeconds` (default 60 s).
- **Thread safety**: `IMClient` is not thread-safe. Use one instance per thread, or
  synchronise externally if sharing.
