import io.github.reapicorn.im.IMClient;
import io.github.reapicorn.im.IMConfig;
import io.github.reapicorn.im.Result;
import io.github.reapicorn.im.model.SystemUser;

/**
 * Example: look up the currently authenticated system user account.
 *
 * GET /systemusers/me — returns the IM system account (eruid, roles, status).
 */
void main() {

    IMClient client = buildClient();

    Result<Void> session = client.initSession();
    if (!session.isSuccess()) {
        System.err.println("Session init failed: " + session.getError());
        return;
    }

    Result<SystemUser> result = client.systemUsers()
            .getCurrentSystemUser("eruid,eraccountstatus,erroles", null);

    if (!result.isSuccess()) {
        System.err.println("Failed: " + result.getError());
        return;
    }

    SystemUser su = result.getValue();
    System.out.println("--- SystemUser ---");
    System.out.println("id         : " + su.getId());
    System.out.println("uid        : " + su.getUid());
    System.out.println("href       : " + su.getHref());
    System.out.println("attributes : " + su.getAttributes());
    System.out.println("toString   : " + su);
}

IMClient buildClient() {
    return new IMClient(IMConfig.builder()
            .baseUrl("https://your-im-host:30943/itim/rest")
            .username("itim manager")
            .password("your-password")
            .trustAllSsl(true)
            .build());
}
