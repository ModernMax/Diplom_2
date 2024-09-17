package client;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public abstract class RestClient {
    public static final String BASE_URI = "https://stellarburgers.nomoreparties.site/api";
    protected RequestSpecification getDefaultRequestSpecification() {
        return given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON);
    }
}