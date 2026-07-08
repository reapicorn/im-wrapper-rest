import io.github.reapicorn.im.IMClient;
import io.github.reapicorn.im.IMConfig;
import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.model.Person;

/**
 * Example: how Result<T> and IMException work on failure.
 *
 * Every library method returns Result<T> — no exceptions are thrown.
 * This example deliberately triggers a 404 to demonstrate the failure path.
 */
void main() {

    IMClient client = buildClient();

    Result<Void> session = client.initSession();
    if (!session.isSuccess()) {
        System.err.println("Session init failed: " + session.getError());
        return;
    }

    // Deliberately fetch a non-existent person to produce a failure Result.
    Result<Person> result = client.people().getPerson("this-id-does-not-exist", null);

    System.out.println("--- IMException ---");
    System.out.println("isSuccess  : " + result.isSuccess());
    System.out.println("statusCode : " + result.getError().getStatusCode());
    System.out.println("message    : " + result.getError().getMessage());
    System.out.println("body       : " + result.getError().getResponseBody());
    System.out.println("toString   : " + result.getError());
}

IMClient buildClient() {
    return new IMClient(IMConfig.builder()
            .baseUrl("https://your-im-host:30943/itim/rest")
            .username("itim manager")
            .password("your-password")
            .trustAllSsl(true)
            .build());
}
