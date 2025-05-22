package io.bootify.bootify_form_jwt_thymeleaf.config;

import io.bootify.bootify_form_jwt_thymeleaf.BootifyFormJwtThymeleafApplication;
import io.bootify.bootify_form_jwt_thymeleaf.role.RoleRepository;
import io.bootify.bootify_form_jwt_thymeleaf.user.UserRepository;
import io.restassured.RestAssured;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;


/**
 * Abstract base class to be extended by every IT test. Starts the Spring Boot context with a
 * Datasource connected to the Testcontainers Docker instance. The instance is reused for all tests,
 * with all data wiped out before each test.
 */
@SpringBootTest(
        classes = BootifyFormJwtThymeleafApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("it")
@Sql({"/data/clearAll.sql", "/data/userData.sql"})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:17.4");
    private static final GenericContainer<?> hazelcastContainer = new GenericContainer<>("hazelcast/hazelcast:5.5-slim");
    private static final GenericContainer<?> oauthMockContainer = new GenericContainer<>("ghcr.io/navikt/mock-oauth2-server:2.1.10");
    public static final String USER = "user";
    public static final String PASSWORD = "Bootify!";
    private static String formSession = null;
    private static String formSocialSession = null;

    static {
        postgreSQLContainer.withReuse(true)
                .start();
        hazelcastContainer.withExposedPorts(5701)
                .withReuse(true)
                .start();
        oauthMockContainer.withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/isalive").forStatusCode(200))
                .withReuse(true)
                .start();
    }

    @LocalServerPort
    public int serverPort;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public RoleRepository roleRepository;

    @PostConstruct
    public void initRestAssured() {
        RestAssured.port = serverPort;
        RestAssured.urlEncodingEnabled = false;
        RestAssured.config = RestAssured.config().sessionConfig(new SessionConfig().sessionIdName("SESSION"));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DynamicPropertySource
    public static void setDynamicProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.provider.oauthMock.issuer-uri",
                () -> "http://" + oauthMockContainer.getHost() + ":" + oauthMockContainer.getMappedPort(8080) + "/oauthMock");
    }

    public String readResource(final String resourceName) {
        try {
            return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
        } catch (final IOException io) {
            throw new UncheckedIOException(io);
        }
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
