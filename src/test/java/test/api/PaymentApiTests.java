package test.api;

import com.codeborne.selenide.logevents.SelenideLogger;
import com.google.gson.Gson;

import data.APIHelper;
import data.DataHelper;
import data.DataHelperSQL;

import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.testng.AssertJUnit.*;

@Epic("API тестирование функционала Путешествие дня")
@Feature("Покупка тура по карте")
public class PaymentApiTests {
    private static DataHelper.CardData cardData;
    private static final Gson gson = new Gson();
    private static final String paymentUrl = "/api/v1/pay";
    private static List<DataHelperSQL.PaymentEntity> payments;
    private static List<DataHelperSQL.CreditRequestEntity> credits;
    private static List<DataHelperSQL.OrderEntity> orders;

    @BeforeClass
    public void setupClass() {
        DataHelperSQL.setDown();
        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true).savePageSource(true));
    }

    @AfterMethod
    public void setDownMethod() {
        DataHelperSQL.setDown();
    }

    @AfterClass
    public void setDownClass() {
        SelenideLogger.removeListener("allure");
    }

    @Story("Пустое body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldStatus400WithEmptyBody() {
        cardData = DataHelper.getValidApprovedCard();
        APIHelper.executeRequest(cardData, paymentUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());
    }

    @Story("Пустое значение у атрибута number в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldStatus400WithEmptyNumber() {
        cardData = new DataHelper.CardData(null, DataHelper.generateMonth(1), DataHelper.generateYear(2),
                DataHelper.generateValidHolder(), DataHelper.generateValidCVC());
        APIHelper.executeRequest500(cardData, paymentUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(0, payments.size());
        assertEquals(0, credits.size());
        assertEquals(0, orders.size());
    }

    @Story("Пустое значение у атрибута month в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldStatus400WithEmptyMonth() {
        cardData = new DataHelper.CardData(DataHelper.getNumberByStatus("approved"), null, DataHelper.generateYear(2),
                DataHelper.generateValidHolder(), DataHelper.generateValidCVC());
        APIHelper.executeRequest(cardData, paymentUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());
    }

    @Story("Пустое значение у атрибута year в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldStatus400WithEmptyYear() {
        cardData = new DataHelper.CardData(DataHelper.getNumberByStatus("approved"), DataHelper.generateMonth(1), null,
                DataHelper.generateValidHolder(), DataHelper.generateValidCVC());
        APIHelper.executeRequest(cardData, paymentUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());
    }

    @Story("Пустое значение у атрибута holder в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldStatus400WithEmptyHolder() {
        cardData = new DataHelper.CardData(DataHelper.getNumberByStatus("approved"), DataHelper.generateMonth(1),
                DataHelper.generateYear(2), null, DataHelper.generateValidCVC());
        APIHelper.executeRequest(cardData, paymentUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());
    }

    @Story("Пустое значение у атрибута cvc в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldStatus400WithEmptyCvc() {
        cardData = new DataHelper.CardData(DataHelper.getNumberByStatus("approved"), DataHelper.generateMonth(1),
                DataHelper.generateYear(2), DataHelper.generateValidHolder(), null);
        APIHelper.executeRequest(cardData, paymentUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());
    }
}