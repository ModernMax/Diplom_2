package step;

import client.OrderClient;
import dto.CreateOrderRequest;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class OrderSteps {

    private final OrderClient orderClient;

    public OrderSteps(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Step("Получить ингредиенты для заказа")
    public ValidatableResponse getIngredientsToOrder() {
        return orderClient.getIngredients()
                .then();
    }

    @Step("Выбрать хэш случайных ингредиентов")
    public List<String> chooseHashOfRandomIngredients(ValidatableResponse ingredientsResponse, int ingredientsCount) {
        Random random = new Random();
        List<String> ingredients = new ArrayList<>();
        ArrayList<HashMap<String, Object>> ingridientsInfo = ingredientsResponse.extract().path("data");
        for (int i = 0; i < ingredientsCount; i++) {
            ingredients.add((String) ingridientsInfo.get(random.nextInt(ingridientsInfo.size())).get("_id"));
        }

        return ingredients;

    }

    @Step("Создать заказ")
    public ValidatableResponse createOrder(List<String> ingredientsList, String accessToken) {
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .ingredients(ingredientsList)
                .build();
        return orderClient.createOrder(createOrderRequest, accessToken).then();
    }

    @Step("Создать заказ без авторизации")
    public ValidatableResponse createOrderWithoutAuth(List<String> ingredientsList) {
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .ingredients(ingredientsList)
                .build();
        return orderClient.createOrderWithoutAuth(createOrderRequest).then();
    }

    @Step("Получить список последних заказов")
    public ValidatableResponse getListOfRecentOrders() {
        return orderClient.getAllOrders().then();
    }

    @Step("Получить список заказов пользователя")
    public ValidatableResponse getListOfUserOrders(String accessToken) {
        return orderClient.getListOfUserOrders(accessToken).then();
    }
    @Step("Получить список заказов пользователя без авторизации")
    public ValidatableResponse getListOfUserOrdersWithoutAuth() {
        return orderClient.getListOfUserOrdersWithoutAuth().then();
    }

    @Step("Поиск заказа по id или номеру")
    public boolean searchOrderByIdAndNumber(String order) {
        List<HashMap<String, Object>> orderList = getListOfRecentOrders().extract().path("orders");
        for (HashMap<String, Object> stringObjectHashMap : orderList) {
            if (order.equals(stringObjectHashMap.get("_id").toString()) || order.equals(stringObjectHashMap.get("number").toString())) {
                return true;
            }
        }
        return false;
    }

    @Step("Поиск заказа в списке заказов пользователя")
    public boolean searchOrderInUserOrdersList(String order, String accessToken) {
        List<HashMap<String, Object>> orderList = getListOfUserOrders(accessToken).extract().path("orders");
        for (HashMap<String, Object> stringObjectHashMap : orderList) {
            if (order.equals(stringObjectHashMap.get("_id").toString()) || order.equals(stringObjectHashMap.get("number").toString())) {
                return true;
            }
        }
        return false;
    }
}