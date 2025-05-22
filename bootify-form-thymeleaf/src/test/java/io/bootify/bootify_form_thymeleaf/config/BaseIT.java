package io.bootify.bootify_form_thymeleaf.config;

import io.bootify.bootify_form_thymeleaf.BootifyFormThymeleafApplication;
import io.bootify.bootify_form_thymeleaf.repos.UserRepository;
import io.restassured.RestAssured;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;


/**
 * Abstract base class to be extended by every IT test. Starts the Spring Boot context with a
 * Datasource connected to the Testcontainers Docker instance. The instance is reused for all tests,
 * with all data wiped out before each test.
 */
@SpringBootTest(
        classes = BootifyFormThymeleafApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("it")
@Sql({"/data/clearAll.sql", "/data/userData.sql"})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:9.2");
    private static final GenericContainer<?> mailpitContainer = new GenericContainer<>("axllent/mailpit:v1.24");
    public static String smtpHost;
    public static Integer smtpPort;
    public static String messagesUrl;
    private static final GenericContainer<?> oauthMockContainer = new GenericContainer<>("ghcr.io/navikt/mock-oauth2-server:2.1.10");
    public static final String USER = "user";
    public static final String PASSWORD = "Bootify!";
    private static String formSession = null;
    private static String formSocialSession = null;

    static {
        mySQLContainer.withUrlParam("serverTimezone", "UTC")
                .withReuse(true)
                .start();
        mailpitContainer.withExposedPorts(1025, 8025)
                .waitingFor(Wait.forLogMessage(".*accessible via.*", 1))
                .withReuse(true)
                .start();
        smtpHost = mailpitContainer.getHost();
        smtpPort = mailpitContainer.getMappedPort(1025);
        messagesUrl = "http://" + smtpHost + ":" + mailpitContainer.getMappedPort(8025) + "/api/v1/messages";
        oauthMockContainer.withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/isalive").forStatusCode(200))
                .withReuse(true)
                .start();
    }

    @LocalServerPort
    public int serverPort;

    @Autowired
    public UserRepository userRepository;

    @PostConstruct
    public void initRestAssured() {
        RestAssured.port = serverPort;
        RestAssured.urlEncodingEnabled = false;
        RestAssured.config = RestAssured.config().sessionConfig(new SessionConfig().sessionIdName("SESSION"));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DynamicPropertySource
    public static void setDynamicProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> smtpHost);
        registry.add("spring.mail.port", () -> smtpPort);
        registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.required", () -> false);
        registry.add("spring.security.oauth2.client.provider.oauthMock.issuer-uri",
                () -> "http://" + oauthMockContainer.getHost() + ":" + oauthMockContainer.getMappedPort(8080) + "/oauthMock");
    }

    @BeforeEach
    public void beforeEach() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .delete(messagesUrl);
    }

    public String readResource(final String resourceName) {
        try {
            return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
        } catch (final IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    public void waitForMessages(final int total) {
        int loop = 0;
        while (loop++ < 25) {
            final Response messagesResponse = RestAssured
                    .given()
                        .accept(ContentType.JSON)
                    .when()
                        .get(messagesUrl);
            if (messagesResponse.jsonPath().getInt("total") == total) {
                return;
            }
            try {
                Thread.sleep(250);
            } catch (final InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        throw new RuntimeException("Could not find " + total + " messages in time.");
    }

    public String formSession() {
        if (formSession == null) {
            // init session
            formSession = RestAssured
                    .given()
                        .accept(ContentType.HTML)
                    .when()
                        .get("/login")
                    .sessionId();

            // perform login
            formSession = RestAssured
                    .given()
                        .sessionId(formSession)
                        .csrf("/login")
                        .accept(ContentType.HTML)
                        .contentType(ContentType.URLENC)
                        .formParam("name", USER)
                        .formParam("password", PASSWORD)
                    .when()
                        .post("/login")
                    .sessionId();
        }
        return formSession;
    }

    public String formSocialSession() {
        return formSocialSession(false);
    }

    public String formSocialSession(final boolean forceNewSession) {
        if (formSocialSession == null || forceNewSession) {
            // start oauth process
            final Response startLoginResult = RestAssured
                    .given()
                        .redirects().follow(false)
                        .accept(ContentType.HTML)
                    .when()
                        .get("/oauth2/authorization/github");
            formSocialSession = startLoginResult.sessionId();
            final String loginFormUrl = startLoginResult.getHeader("Location");

            // load login form
            final Response loginFormResult = RestAssured
                    .given()
                        .accept(ContentType.HTML)
                    .when()
                        .get(loginFormUrl);

            // submit login form
            final String completionUrl = RestAssured
                    .given()
                        .redirects().follow(false)
                        .accept(ContentType.HTML)
                        .contentType(ContentType.URLENC)
                        .cookies(loginFormResult.cookies())
                        .formParam("username", USER)
                        .formParam("claims", "{\"id\": \"" + USER.hashCode() + "\"}")
                    .when()
                        .post(loginFormUrl)
                    .getHeader("Location");

            // complete oauth process
            formSocialSession = RestAssured
                    .given()
                        .redirects().follow(false)
                        .sessionId(formSocialSession)
                        .accept(ContentType.HTML)
                    .when()
                        .get(completionUrl)
                    .sessionId();
        }
        return formSocialSession;
    }

}
