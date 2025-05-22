package io.bootify.bootify_keycloak_rest.config;

import com.redis.testcontainers.RedisContainer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.bootify.bootify_keycloak_rest.BootifyKeycloakRestApplication;
import io.bootify.bootify_keycloak_rest.repos.UserRepository;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.config.SessionConfig;
import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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
import org.testcontainers.containers.MySQLContainer;


/**
 * Abstract base class to be extended by every IT test. Starts the Spring Boot context with a
 * Datasource connected to the Testcontainers Docker instance. The instance is reused for all tests,
 * with all data wiped out before each test.
 */
@SpringBootTest(
        classes = BootifyKeycloakRestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("it")
@Sql("/data/clearAll.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:9.2");
    private static final RedisContainer redisContainer = new RedisContainer("redis:7.4-alpine");
    private static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.0.5");
    public static final String USER = "user@invalid.bootify.io";
    public static final String PASSWORD = "Bootify!";
    private static String keycloakToken = null;

    static {
        mySQLContainer.withUrlParam("serverTimezone", "UTC")
                .withReuse(true)
                .start();
        redisContainer.withExposedPorts(6379)
                .withReuse(true)
                .start();
        keycloakContainer.withRealmImportFile("keycloak-realm.json")
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
        registry.add("spring.data.redis.url", () -> redisContainer.getRedisURI());
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/test-realm/protocol/openid-connect/certs");
    }

    public String readResource(final String resourceName) {
        try {
            return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
        } catch (final IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    public String keycloakToken() {
        if (keycloakToken == null) {
            // get a fresh token
            final String tokenUrl = keycloakContainer.getAuthServerUrl() + "/realms/test-realm/protocol/openid-connect/token";
            final Map<String, Object> keycloakTokenResponse = RestAssured
                    .given()
                        .accept(ContentType.JSON)
                        .contentType(ContentType.URLENC)
                        .formParam("grant_type", "password")
                        .formParam("client_id", "test-client")
                        .formParam("client_secret", "B3D9B63A0859BDBC559C2F6FC8B79C35")
                        .formParam("username", USER)
                        .formParam("password", PASSWORD)
                    .when()
                        .post(tokenUrl)
                    .body().as(new TypeRef<>() {
                    });
            keycloakToken = "Bearer " + keycloakTokenResponse.get("access_token");
        }
        return keycloakToken;
    }

}
