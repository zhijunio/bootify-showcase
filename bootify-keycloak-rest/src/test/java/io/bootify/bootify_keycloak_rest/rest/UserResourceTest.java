package io.bootify.bootify_keycloak_rest.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.bootify.bootify_keycloak_rest.config.BaseIT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;


public class UserResourceTest extends BaseIT {

    @Test
    @Sql("/data/userData.sql")
    void getAllUsers_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/users")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(3))
                    .body("content.get(0).id", Matchers.equalTo(1000));
    }

    @Test
    @Sql("/data/userData.sql")
    void getAllUsers_filtered() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/users?filter=1001")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(1))
                    .body("content.get(0).id", Matchers.equalTo(1001));
    }

    @Test
    void getAllUsers_unauthorized() {
        RestAssured
                .given()
                    .redirects().follow(false)
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/users")
                .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("code", Matchers.equalTo("AUTHORIZATION_DENIED"));
    }

    @Test
    @Sql("/data/userData.sql")
    void getUser_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/users/1000")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("name", Matchers.equalTo("Sed diam voluptua."));
    }

    @Test
    void getUser_notFound() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/users/1666")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", Matchers.equalTo("NOT_FOUND"));
    }

    @Test
    void createUser_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/userDTORequest.json"))
                .when()
                    .post("/api/users")
                .then()
                    .statusCode(HttpStatus.CREATED.value());
        assertEquals(2, userRepository.count());
    }

    @Test
    void createUser_missingField() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/userDTORequest_missingField.json"))
                .when()
                    .post("/api/users")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", Matchers.equalTo("VALIDATION_FAILED"))
                    .body("fieldErrors.get(0).property", Matchers.equalTo("name"))
                    .body("fieldErrors.get(0).code", Matchers.equalTo("REQUIRED_NOT_NULL"));
    }

    @Test
    @Sql("/data/userData.sql")
    void updateUser_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/userDTORequest.json"))
                .when()
                    .put("/api/users/1000")
                .then()
                    .statusCode(HttpStatus.OK.value());
        assertEquals("Duis autem vel.", userRepository.findById(((long)1000)).orElseThrow().getName());
        assertEquals(3, userRepository.count());
    }

    @Test
    @Sql("/data/userData.sql")
    void deleteUser_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, keycloakToken())
                    .accept(ContentType.JSON)
                .when()
                    .delete("/api/users/1000")
                .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        assertEquals(2, userRepository.count());
    }

}
