package com.devsuperior.dscommerce.controllers;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;

    @BeforeEach
    public void setUp() {
        baseURI = "http://localhost:8080";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = "invalidToken";

    }

    @Test
    public void findUserShouldReturnUnauthorizedWhenNotLoggedIn() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
                .when()
                .get("/users/me")
                .then()
                .statusCode(401);
    }

    @Test
    public void findUserShouldReturnDataWhenAdminIsLoggedIn() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("id", is(2))
                .body("name", equalTo("Alex Green"))
                .body("email", equalTo("alex@gmail.com"))
                .body("phone", equalTo("977777777"))
                .body("roles", hasItems("ROLE_ADMIN"))
                .body("brithDate", equalTo("1987-12-13"));
    }

    @Test
    public void findUserShouldReturnDataWhenClientIsLoggedIn() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", equalTo("Maria Brown"))
                .body("email", equalTo("maria@gmail.com"))
                .body("phone", equalTo("988888888"))
                .body("roles", hasItems("ROLE_CLIENT"))
                .body("brithDate", equalTo("2001-07-25"));
    }
}
