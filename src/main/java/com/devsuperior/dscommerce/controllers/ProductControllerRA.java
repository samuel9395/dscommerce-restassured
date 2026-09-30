package devsuperior.dscommerce.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ProductControllerRA {

    private Long existingProductId, nonExistingProductId;

    @BeforeEach
    public void setUp() {
        baseURI = "http://localhost:8080";
    }

    @Test
    public void findByshouldReturnProductWhenIdExists() {
        existingProductId = 2L;
        String imgUrl = "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg";

        given().get("/products/{id}", existingProductId)
                .then().statusCode(200)
                .body("id", is(2))
                .body("name", equalTo("Smart TV"))
                .body("imgUrl", equalTo(imgUrl))
                .body("price", is(2190.0f))
                .body("categories.id", hasItems(2, 3))
                .body("categories.name", hasItems("Eletrônicos", "Computadores"));
    }
}