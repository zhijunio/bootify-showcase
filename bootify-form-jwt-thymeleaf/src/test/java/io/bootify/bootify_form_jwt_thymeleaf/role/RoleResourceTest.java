package io.bootify.bootify_form_jwt_thymeleaf.role;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.bootify.bootify_form_jwt_thymeleaf.config.BaseIT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;


public class RoleResourceTest extends BaseIT {

    @Test
    @Sql("/data/roleData.sql")
    void getAllRoles_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/roles")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(2))
                    .body("content.get(0).id", Matchers.equalTo(1100));
    }

    @Test
    @Sql("/data/roleData.sql")
    void getAllRoles_filtered() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/roles?filter=1101")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(1))
                    .body("content.get(0).id", Matchers.equalTo(1101));
    }

    @Test
    @Sql("/data/roleData.sql")
    void getRole_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/roles/1100")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("name", Matchers.equalTo("Sed diam voluptua."));
    }

    @Test
    void getRole_notFound() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/roles/1766")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", Matchers.equalTo("NOT_FOUND"));
    }

    @Test
    void createRole_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/roleDTORequest.json"))
                .when()
                    .post("/api/roles")
                .then()
                    .statusCode(HttpStatus.CREATED.value());
        assertEquals(1, roleRepository.count());
    }

    @Test
    void createRole_missingField() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/roleDTORequest_missingField.json"))
                .when()
                    .post("/api/roles")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", Matchers.equalTo("VALIDATION_FAILED"))
                    .body("fieldErrors.get(0).property", Matchers.equalTo("name"))
                    .body("fieldErrors.get(0).code", Matchers.equalTo("REQUIRED_NOT_NULL"));
    }

    @Test
    @Sql("/data/roleData.sql")
    void updateRole_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/roleDTORequest.json"))
                .when()
                    .put("/api/roles/1100")
                .then()
                    .statusCode(HttpStatus.OK.value());
        assertEquals("Duis autem vel.", roleRepository.findById(((long)1100)).orElseThrow().getName());
        assertEquals(2, roleRepository.count());
    }

    @Test
    @Sql("/data/roleData.sql")
    void deleteRole_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .delete("/api/roles/1100")
                .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        assertEquals(1, roleRepository.count());
    }

}
