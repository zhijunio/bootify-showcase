package io.bootify.bootify_keycloak_rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.bootify.bootify_keycloak_rest.config.BaseIT;
import io.bootify.bootify_keycloak_rest.domain.User;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;


public class UserSynchronizationServiceTest extends BaseIT {

    @Test
    public void userCreatedAfterLogin() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                .when()
                    .get("/");
        ;
        final User user = userRepository.findByOpenid("7b529f29-bc0e-3988-b016-14dc83035fc7");
        assertNotNull(user);
        assertEquals("user@invalid.bootify.io", user.getEmail());
        assertEquals("Bob", user.getFirstNname());
        assertEquals("Lazar", user.getLastNamme());
    }

}
