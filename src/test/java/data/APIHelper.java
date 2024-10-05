package data;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class APIHelper{
    private static final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(8080)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    public static void executeRequest(Object requestBody, String endpoint) {
        Gson gson = new Gson();
        var body = gson.toJson(requestBody);

        given()
                .spec(spec)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .statusCode(200);
    }
    public static void executeRequest500(Object requestBody, String endpoint) {
        Gson gson = new Gson();
        var body = gson.toJson(requestBody);

        given()
                .spec(spec)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .statusCode(500);
    }

}