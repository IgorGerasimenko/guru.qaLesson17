
import com.codeborne.selenide.WebDriverRunner;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.*;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


public class DemoWebShopTests {

    String email = "planetz@mail.ru";
    String password = "qwerty123";
    String productCount;
    private Response response;
    Map<String, String> authorizationCookie;

    @BeforeAll
    static void configureBaseUrl() {
        RestAssured.baseURI = "http://demowebshop.tricentis.com/";
    }

    @Test
    @DisplayName("Successful authorization to some demowebshop (API + UI)")
    void loginWithCookieTest() {
//        open("http://demowebshop.tricentis.com/");

        step("Получим Куки", () -> {

            authorizationCookie =

                    given()
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .formParam("Email", email)
                            .formParam("Password", password)
                            .when()
                            .post("/login")
                            .then()
                            .statusCode(302)
                            .extract()
                            .cookies();
        });


        step("Добавим товар в корзину и проверим количество товара в ней", () -> {
            response =
                    given()
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .body("product_attribute_74_5_26=81&product_attribute_74_6_27=83&product_attribute_74_3_28=86&addtocart_74.EnteredQuantity=1")
                            .cookies(authorizationCookie)
                            .when()
                            .post("/addproducttocart/details/74/1")
                            .then()
                            .statusCode(200)
                            .body("success", is(true))
                            .body("message", is("The product has been added to your <a href=\"/cart\">shopping cart</a>"))
                            .extract().response();

            String responseParam = response.path("updatetopcartsectionhtml").toString();
            productCount = responseParam.substring(1, responseParam.length() - 1);

        });

        step("Проверим через UI, кол-во товара в корзине верное", () -> {

            open("http://demowebshop.tricentis.com/content/images/thumbs/0000201_build-your-own-expensive-computer_80.jpeg");

            HashMap<String, String> coockies = new HashMap<String, String>(authorizationCookie);

            for (Map.Entry<String, String> entry : coockies.entrySet()) {
                WebDriverRunner.getWebDriver().manage().addCookie(new Cookie(entry.getKey(), entry.getValue()));
            }

            open("http://demowebshop.tricentis.com/cart");

            $("input[name=\"itemquantity1904879\"]").shouldBe(value(productCount));

        });
    }


}