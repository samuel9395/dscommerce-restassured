package devsuperior.dscommerce.controllers;

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

public class ProductControllerRA {

    private Long existingProductId, nonExistingProductId;
    private String productName;
    private Map<String, Object> postProductInstance;

    @BeforeEach
    public void setUp() {
        baseURI = "http://localhost:8080";
        productName = "Macbook Pro";

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Produto novo");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore.");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 200.0);

        List<Map<String, Object>> categories = new ArrayList<>();
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);
        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);
        categories.add(category1);
        categories.add(category2);
        postProductInstance.put("categories", categories);
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

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsEmpty() {

        given().get("/products?page=0")
                .then().statusCode(200)
                .body("content.name", hasItems("Macbook Pro", "PC Gamer", "PC Gamer Alfa"));
    }

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsNotEmpty() {

        given().get("/products?name={productName}", productName)
                .then().statusCode(200)
                .body("content.id[0]", is(3))
                .body("content.name[0]", equalTo("Macbook Pro"))
                .body("content.price[0]", is(1250.0f))
                .body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
    }

    @Test
    public void findAllShouldReturnPagedPRoductsWithPriceGreaterThen2000() {

        given().get("/products?size=25")
                .then().statusCode(200)
                .body("content.findAll {it.price > 2000}.name", hasItems("Smart TV", "PC Gamer Weed"));

    }

    @Test
    public void insertShoulReturnProductCreatedWhenAdminLogged() {
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);
        String adminToken = "";

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(201)
                .body("name",  equalTo("Produto novo"))
                .body("price", is(200.0f))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
                .body("categories.id", hasItems(2, 3));

    }
}