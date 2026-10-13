package com.devsuperior.dscommerce.controllers;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;
    private Long existingOrderId, nonExistingOrderId;
    private Map<String, Object> postOrderInstance;

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

        existingOrderId = 1L;
        nonExistingOrderId = 1000L;

        // inicializa a instância antes de popular os dados
        postOrderInstance = new HashMap<>();

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", 1);
        item1.put("quantity", 2);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("productId", 3);
        item2.put("quantity", 1);

        items.add(item1);
        items.add(item2);
        postOrderInstance.put("items", items);

    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistsAndAdminLogged() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items.productId", hasItems(1, 3))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
                .body("total", is(1431.0f));
    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistsAndClientLogged() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items.productId", hasItems(1, 3))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
                .body("total", is(1431.0f));
    }

    @Test
    public void findByIdShouldReturnForbiddenWhenIdExistsAndClientLoggedAndOrderDoesNotBelongUser() {
        Long otherOrderId = 2L;

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", otherOrderId)
                .then()
                .statusCode(403);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndAdminLogged() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Order not found"));
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndClientLogged() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Order not found"));
    }

    @Test
    public void findByIdShouldReturnUnauthorizedWhenIdExistsAndInvalidToken() {

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(401);
    }

    @Test
    public void insertShouldReturnOrderCreateWhenClientLogged() {
        JSONObject newProduct = new JSONObject(postOrderInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/orders")
                .then().statusCode(201)
                .body("items.productId", hasItems(1, 3))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenClientLogged() {
        postOrderInstance.put("items", new ArrayList<>()); // Remove all items from the order

        JSONObject newProduct = new JSONObject(postOrderInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/orders")
                .then().statusCode(422)
            .body("errors.message[0]", equalTo("Deve ter pelo menos um pedido no item"));
    }

    @Test
    public void insertShouldReturnForbiddenWhenAdminLogged() {
        JSONObject newProduct = new JSONObject(postOrderInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/orders")
                .then().statusCode(403);
    }

    @Test
    public void insertShouldReturnUnauthorizedWhenInvalidToken() {
        JSONObject newProduct = new JSONObject(postOrderInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/orders")
                .then().statusCode(401);
    }
}
