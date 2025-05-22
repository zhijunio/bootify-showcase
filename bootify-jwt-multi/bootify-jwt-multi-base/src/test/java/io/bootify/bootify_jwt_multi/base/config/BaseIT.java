package io.bootify.bootify_jwt_multi.base.config;

import io.bootify.bootify_jwt_multi.base.BootifyJwtMultiApplication;
import io.bootify.bootify_jwt_multi.base.role.repos.RoleRepository;
import io.bootify.bootify_jwt_multi.base.user.repos.UserRepository;
import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.PostgreSQLContainer;


/**
 * Abstract base class to be extended by every IT test. Starts the Spring Boot context with a
 * Datasource connected to the Testcontainers Docker instance. The instance is reused for all tests,
 * with all data wiped out before each test.
 */
@SpringBootTest(
        classes = BootifyJwtMultiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("it")
@Sql({"/data/clearAll.sql", "/data/userData.sql"})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:17.4");

    static {
        postgreSQLContainer.withReuse(true)
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
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public String readResource(final String resourceName) {
        try {
            return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
        } catch (final IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    public String jwtToken() {
        // user user, expires 2040-01-01
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiJ1c2VyIiwibG9naW5fdHlwZSI6ImRpcmVjdCIsInJvbGVzIjpbIlVTRVIiXSwiaXNzIjoiYm9vdGlmeSIsImlhdCI6MTc0Nzg4MjI2OSwiZXhwIjoyMjA4OTg4ODAwfQ." +
                "I7fEWAS6vAZVCbuVO6kyKHpBFPuixjya_UhZeryCtPHNmE2mFYs1Rn9606ExWoIuQTXg-kUsd6JZoUvDsZABDYaZDcIjHRd6pxaW_fM5p8oPZI9Ngvj8hNL9N_Yim6M_wxdZn00ONzOk8E1XfH_1N9x7Tt9n4xXqDAgj0gJo-kql0_2BLfJDKkfA_LBhhBzkf1kefiJY5-_Qczez9qL84k2PCmE94ULgtTXVFw89mjLqapBG72nxI0eN79RIx-xWIMUfoUKAoEkykUx0AGD8DOilgQvA7VR8Sd5u-InbmiC5xVNJWpKioHuUqd51ha3Z7GjOD7MPkUpt81sANuDuUQ";
    }

}
