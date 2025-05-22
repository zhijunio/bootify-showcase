package io.bootify.bootify_baisc.config;

import io.bootify.bootify_baisc.BootifyBaiscApplication;
import io.bootify.bootify_baisc.repos.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
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
        classes = BootifyBaiscApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("it")
@Sql("/data/clearAll.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:9.2");
    private static final GenericContainer<?> mailpitContainer = new GenericContainer<>("axllent/mailpit:v1.24");
    public static String smtpHost;
    public static Integer smtpPort;
    public static String messagesUrl;
    public static final String USER = "user";
    public static final String PASSWORD = "Bootify!";

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
    }

    @LocalServerPort
    public int serverPort;

    @Autowired
    public UserRepository userRepository;

    @PostConstruct
    public void initRestAssured() {
        RestAssured.port = serverPort;
        RestAssured.urlEncodingEnabled = false;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DynamicPropertySource
    public static void setDynamicProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> smtpHost);
        registry.add("spring.mail.port", () -> smtpPort);
        registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.required", () -> false);
    }

    @BeforeEach
    public void beforeEach() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .delete(messagesUrl);
    }

    @SneakyThrows
    public String readResource(final String resourceName) {
        return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }

    @SneakyThrows
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
            Thread.sleep(250);
        }
        throw new RuntimeException("Could not find " + total + " messages in time.");
    }

}
