package devsuperior.dscommerce.controllers;

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

public class ProductControllerRA {

    private Long existingProductId, nonExistingProductId, dependentProductId;
    private String productName;
    private Map<String, Object> postProductInstance;
    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;

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

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = "invalidToken";
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

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(201)
                .body("name", equalTo("Produto novo"))
                .body("price", is(200.0f))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
                .body("categories.id", hasItems(2, 3));

    }

    @Test
    public void insertShoulReturnUnprocessableEntityWhenAdminLoggedAndInvalidName() {
        postProductInstance.put("name", "ab");
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("Required 3 to 80 characters"));
    }

    @Test
    public void insertShoulReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription() {
        postProductInstance.put("description", "short");
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("The description must be at least 10 characters long."));
    }

    @Test
    public void insertShoulReturnUnprocessableEntityWhenAdminLoggedAndPriceIsNegative() {
        postProductInstance.put("price", "-200.0f");
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("Required positive price"));
    }

    @Test
    public void insertShoulReturnUnprocessableEntityWhenAdminLoggedAndPriceIsZero() {
        postProductInstance.put("price", "0.0f");
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("Required positive price"));
    }

    @Test
    public void insertShoulReturnUnprocessableEntityWhenAdminLoggedAndProductHasNoCategory() {
        postProductInstance.put("categories", null);
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("At least one category is required."));
    }

    @Test
    public void insertShouldReturnForbiddenWhenClientLogged() {
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(403);
    }

    @Test
    public void insertShouldReturnUnathorizedWhenInvalidToken() {
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/products")
                .then().statusCode(401);
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExistsAndAdminLogged() {
        existingProductId = 25L;

        given().header("Authorization", "Bearer " + adminToken)
                .when().delete("/products/{id}", existingProductId)
                .then().statusCode(204);
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExistAndAdminLogged() {
        nonExistingProductId = 100L;

        given().header("Authorization", "Bearer " + adminToken)
                .when().delete("/products/{id}", nonExistingProductId)
                .then().statusCode(404)
                .body("error", equalTo("Resource not found!"))
                .body("status", equalTo(404));
    }

    @Test
    public void deleteShouldReturnConflictWhenDependentIdAndAdminLogged() {
        dependentProductId = 3L;

        given().header("Authorization", "Bearer " + adminToken)
                .when().delete("/products/{id}", dependentProductId)
                .then().statusCode(409);
    }

    @Test
    public void deleteShouldReturnForbiddenWhenClientLogged() {
        existingProductId = 3L;

        given().header("Authorization", "Bearer " + clientToken)
                .when().delete("/products/{id}", existingProductId)
                .then().statusCode(403);
    }

    @Test
    public void deleteShouldReturnUnauthorizedWhenInvalidToken() {
        existingProductId = 3L;

        given().header("Authorization", "Bearer " + invalidToken)
                .when().delete("/products/{id}", existingProductId)
                .then().statusCode(401);
    }

    @Test
    public void updateShoulReturnProductWhenAdminLogged() {
        existingProductId = 1L;

        postProductInstance.put("price", "500.0f");
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(200)
                .body("price", is(500.0f));
    }

    @Test
    public void updateShoulReturnNotFoundWhenAdminLogged() {
        nonExistingProductId = 100L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", nonExistingProductId)
                .then().statusCode(404);
    }

    @Test
    public void updateShoulReturnUnprocessableEntityWhenAdminLogged() {
        postProductInstance.put("name", "ab");
        existingProductId = 2L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("Required 3 to 80 characters"));
    }

    @Test
    public void updateShoulReturnUnprocessableEntityWhenDescriptionIsInvalid() {
        postProductInstance.put("description", "ab");
        existingProductId = 3L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("The description must be at least 10 characters long."));
    }

    @Test
    public void updateShoulReturnUnprocessableEntityWhenPriceIsNegative() {
        postProductInstance.put("price", -10.0f);
        existingProductId = 4L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("Required positive price"));
    }

    @Test
    public void updateShoulReturnUnprocessableEntityWhenPriceIsZero() {
        postProductInstance.put("price", 0.0f);
        existingProductId = 5L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("Required positive price"));
    }

    @Test
    public void updateShoulReturnUnprocessableEntityWhenAdminLoggedAndHasNoCategory() {
        postProductInstance.put("categories", null);
        existingProductId = 6L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(422)
                .body("errors.message[0]", equalTo("At least one category is required."));
    }

    @Test
    public void updateShoulReturnForbiddenWhenClientLogged() {
        existingProductId = 7L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(403);
    }

    @Test
    public void updateShoulReturnUnauthorizedWhenClientLogged() {
        existingProductId = 8L;
        // converte em json para inserir novo produto
        JSONObject newProduct = new JSONObject(postProductInstance);

        given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().put("/products/{id}", existingProductId)
                .then().statusCode(401);
    }
}