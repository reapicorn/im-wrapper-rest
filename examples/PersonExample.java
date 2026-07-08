import io.github.reapicorn.im.IMClient;
import io.github.reapicorn.im.IMConfig;
import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.model.Person;

/**
 * Example: look up the currently authenticated person.
 *
 * GET /people/me — returns the IM person record associated with the
 * authenticated user (cn, sn, mail, uid, and other directory attributes).
 */
void main() {

    IMClient client = buildClient();

    Result<Void> session = client.initSession();
    if (!session.isSuccess()) {
        System.err.println("Session init failed: " + session.getError());
        return;
    }

    Result<Person> result = client.people()
            .getCurrentPerson("cn,sn,mail,uid");

    if (!result.isSuccess()) {
        System.err.println("Failed: " + result.getError());
        return;
    }

    Person p = result.getValue();
    System.out.println("--- Person ---");
    System.out.println("id         : " + p.getId());
    System.out.println("name       : " + p.getName());
    System.out.println("href       : " + p.getHref());
    System.out.println("attributes : " + p.getAttributes());
    System.out.println("toString   : " + p);
}

IMClient buildClient() {
    return new IMClient(IMConfig.builder()
            .baseUrl("https://your-im-host:30943/itim/rest")
            .username("itim manager")
            .password("your-password")
            .trustAllSsl(true)
            .build());
}
