import io.github.reapicorn.im.IMClient;
import io.github.reapicorn.im.IMConfig;
import io.github.reapicorn.im.Result;

/**
 * Minimal example: configure the client and establish a session.
 *
 * Run with JDK 25+ (instance main method / unnamed class):
 *   javac --release 25 -cp im-wrapper.jar GettingStartedExample.java
 *   java  -cp im-wrapper.jar:. GettingStartedExample
 */
void main() {

    IMConfig config = IMConfig.builder()
            .baseUrl("https://your-im-host:30943/itim/rest")
            .username("itim manager")
            .password("your-password")
            .trustAllSsl(true)          // use sslContext() in production
            .connectTimeoutSeconds(30)
            .readTimeoutSeconds(60)
            .build();

    IMClient client = new IMClient(config);

    Result<Void> session = client.initSession();
    if (!session.isSuccess()) {
        System.err.println("Session init failed: " + session.getError());
        return;
    }

    System.out.println("--- Session ---");
    System.out.println("isSuccess      : true");
    System.out.println("baseUrl        : " + client.getConfig().getBaseUrl());
    System.out.println("username       : " + client.getConfig().getUsername());
    System.out.println("trustAllSsl    : " + client.getConfig().isTrustAllSsl());
    System.out.println("connectTimeout : " + client.getConfig().getConnectTimeoutSeconds() + "s");
    System.out.println("readTimeout    : " + client.getConfig().getReadTimeoutSeconds() + "s");
}
