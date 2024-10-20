package test.api;

import com.codeborne.selenide.logevents.SelenideLogger;
import com.google.gson.Gson;

import data.APIHelper;
import data.DataHelper;
import data.DataHelperSQL;

import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@Epic("API тестирование функционала Путешествие дня")
@Feature("Покупка тура в кредит")
public class CreditApiTests {
    private static DataHelper.CardData cardData;
    private static final Gson gson = new Gson();
    private static final String creditUrl = "/api/v1/credit";
    private static List<DataHelperSQL.PaymentEntity> payments;
    private static List<DataHelperSQL.CreditRequestEntity> credits;
    private static List<DataHelperSQL.OrderEntity> orders;

    @BeforeClass
    public void initializeTest() {
        DataHelperSQL.clearDatabaseRecords();
        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true).savePageSource(true));
    }
    @Story("Пустое значение у атрибута holder в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void testEmptyHolderCauses400() {
        cardData = new DataHelper.CardData(DataHelper.generateCardNumberByStatus("approved"), DataHelper.generateMonth(1),
                DataHelper.generateYear(2), null, DataHelper.generateValidCVC());
        APIHelper.executeRequest(cardData, creditUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());
    }

    @Story("Пустое значение у атрибута cvc в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void testEmptyCvcCauses400() {
        cardData = new DataHelper.CardData(DataHelper.generateCardNumberByStatus("approved"), DataHelper.generateMonth(1),
                DataHelper.generateYear(2), DataHelper.generateValidHolder(), null);
        APIHelper.executeRequest(cardData, creditUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());
    }

    @Story("Пустое значение у атрибута number в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void testEmptyNumberCauses400() {
        cardData = new DataHelper.CardData(null, DataHelper.generateMonth(1), DataHelper.generateYear(2),
                DataHelper.generateValidHolder(), DataHelper.generateValidCVC());
        APIHelper.executeRequest500(cardData, creditUrl);
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
    public void testEmptyMonthCauses400() {
        cardData = new DataHelper.CardData(DataHelper.generateCardNumberByStatus("approved"), null, DataHelper.generateYear(2),
                DataHelper.generateValidHolder(), DataHelper.generateValidCVC());
        APIHelper.executeRequest(cardData, creditUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());
    }

    @Story("Пустое значение у атрибута year в body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void testEmptyYearCauses400() {
        cardData = new DataHelper.CardData(DataHelper.generateCardNumberByStatus("approved"), DataHelper.generateMonth(1), null,
                DataHelper.generateValidHolder(), DataHelper.generateValidCVC());
        APIHelper.executeRequest(cardData, creditUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());
    }
    @Story("Пустое body запроса")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void testEmptyRequestBodyCauses400() {
        cardData = DataHelper.generateApprovedCardData();
        APIHelper.executeRequest(cardData, creditUrl);
        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());
    }

    @AfterMethod
    public void tearDownMethod() {
        DataHelperSQL.clearDatabaseRecords();
    }

    @AfterClass
    public void tearDownClass() {
        SelenideLogger.getReadableSubject("allure");
    }
}