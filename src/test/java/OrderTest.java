import client.OrderClient;
import client.UserClient;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import step.OrderSteps;
import step.UserSteps;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;


public class OrderTest {
    private OrderSteps orderSteps;
    private UserSteps userSteps;
    private String name;
    private String password;
    private String email;

    @Before
    @Step("Подготовка тестовых данных")
    public void setUp() {
        orderSteps = new OrderSteps(new OrderClient());
        userSteps = new UserSteps(new UserClient());
        name = RandomStringUtils.randomAlphabetic(10);
        password = RandomStringUtils.randomAlphabetic(10);
        email = RandomStringUtils.randomAlphabetic(10) + "@mail.test";

    }

    @After
    @Step("Удаление пользователя")
    public void tearDown() {
        try {
            userSteps.deleteUser(email, password);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    @DisplayName("Получить список ингредиентов")
    public void getIngredientsList() {
        orderSteps.getIngredientsToOrder().assertThat().body("data", notNullValue()).and().body("data", isA(ArrayList.class));
    }

    @Test
    @DisplayName("Создать заказ не авторизированным пользователем")
    public void createOrderByUnauthorizedUser() {
        ValidatableResponse ingredientsInfo = orderSteps.getIngredientsToOrder();
        List<String> ingredientsList = orderSteps.chooseHashOfRandomIngredients(ingredientsInfo, 5);
        orderSteps.createOrderWithoutAuth(ingredientsList).assertThat().body("success", equalTo(true));

    }

    @Test
    @DisplayName("Создать заказ авторизированным пользователем")
    public void createOrderByAuthorizedUser() {
        userSteps.createUser(email, password, name);
        String accessToken = userSteps.getUserToken(email, password);
        ValidatableResponse ingredientsInfo = orderSteps.getIngredientsToOrder();
        List<String> ingredientsList = orderSteps.chooseHashOfRandomIngredients(ingredientsInfo, 5);
        orderSteps.createOrder(ingredientsList, accessToken).assertThat().body("success", equalTo(true));

    }

    @Test
    @DisplayName("Создать заказ с пустым списком ингредиентов")
    public void createOrderWithEmptyListOfIngredients() {

        orderSteps.createOrderWithoutAuth(new ArrayList<>()).assertThat().statusCode(SC_BAD_REQUEST);

    }

    @Test
    @DisplayName("Создать заказ с неправильным списком ингредиентов")
    public void createOrderWithIncorrectListOfIngredients() {
        ArrayList<String> invalidIngredient = new ArrayList<>();
        invalidIngredient.add("invalidIngredient");
        orderSteps.createOrderWithoutAuth(invalidIngredient).assertThat().statusCode(SC_INTERNAL_SERVER_ERROR);

    }

    @Test
    @DisplayName("Получить сделанный без авторизации заказ из списка всех заказов")
    public void getOrderMadeWithoutAuthFromListOfOrders() {
        ValidatableResponse ingredientsInfo = orderSteps.getIngredientsToOrder();
        List<String> ingredientsList = orderSteps.chooseHashOfRandomIngredients(ingredientsInfo, 2);
        String orderId = orderSteps.createOrderWithoutAuth(ingredientsList)
                .extract()
                .path("order.number")
                .toString();
        Assert.assertFalse("Заказ, сделанный без авторизации не должен отображаться в общем списке", orderSteps.searchOrderByIdAndNumber(orderId));
    }


    @Test
    @DisplayName("Получить сделанный после авторизацией заказ из списка всех заказов")
    public void getOrderMadeAfterAuthFromListOfOrders() {
        ValidatableResponse ingredientsInfo = orderSteps.getIngredientsToOrder();
        List<String> ingredientsList = orderSteps.chooseHashOfRandomIngredients(ingredientsInfo, 2);
        userSteps.createUser(email, password, name);
        String accessToken = userSteps.getUserToken(email, password);
        String orderId = orderSteps.createOrder(ingredientsList, accessToken)
                .extract()
                .path("order.number")
                .toString();
        Assert.assertTrue("Заказ, сделанный c авторизацией должен отображаться в общем списке", orderSteps.searchOrderByIdAndNumber(orderId));
    }

    @Test
    @DisplayName("Получить сделанный после авторизацией заказ из списка заказов пользователя")
    public void getOrderMadeAfterAuthFromUserOrderList() {
        ValidatableResponse ingredientsInfo = orderSteps.getIngredientsToOrder();
        List<String> ingredientsList = orderSteps.chooseHashOfRandomIngredients(ingredientsInfo, 2);
        userSteps.createUser(email, password, name);
        String accessToken = userSteps.getUserToken(email, password);
        String orderId = orderSteps.createOrder(ingredientsList, accessToken)
                .extract()
                .path("order.number")
                .toString();
        Assert.assertTrue("Заказ, сделанный с авторизацией должен отображаться в списке заказов пользователя", orderSteps.searchOrderInUserOrdersList(orderId, accessToken));
    }

    @Test
    @DisplayName("Получить список заказов пользователя без авторизации")
    public void getListOfUserOrdersWithoutAuth() {
        orderSteps.getListOfUserOrdersWithoutAuth()
                .assertThat()
                .statusCode(SC_UNAUTHORIZED);
    }
}