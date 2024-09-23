package client;

import dto.CreateOrderRequest;
import io.restassured.response.Response;

public class OrderClient extends RestClient {

    public Response getIngredients() {
        return getDefaultRequestSpecification()
                .when()
                .get("/ingredients");
    }

    public Response createOrder(CreateOrderRequest createOrderRequest, String accessToken) {
        return getDefaultRequestSpecification()
                .header("Authorization", accessToken)
                .body(createOrderRequest)
                .when()
                .post("/orders");
    }

    public Response createOrderWithoutAuth(CreateOrderRequest createOrderRequest) {
        return getDefaultRequestSpecification()
                .body(createOrderRequest)
                .when()
                .post("/orders");
    }

    public Response getListOfUserOrdersWithoutAuth() {
        return getDefaultRequestSpecification()
                .when()
                .get("/orders");
    }

    public Response getListOfUserOrders(String accessToken) {
        return getDefaultRequestSpecification()
                .header("authorization", accessToken)
                .when()
                .get("/orders");
    }

    public Response getAllOrders() {
        return getDefaultRequestSpecification()
                .when()
                .get("/orders/all");
    }

}