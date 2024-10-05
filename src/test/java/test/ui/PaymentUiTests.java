package test.ui;

import com.codeborne.selenide.logevents.SelenideLogger;
import data.DataHelper;
import data.DataHelperSQL;
import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;
import org.testng.annotations.*;
import page.TripCardPage;
import page.TripFormPage;
import java.util.List;
import static com.codeborne.selenide.Selenide.open;
import static org.testng.AssertJUnit.*;


@Epic("UI тестирование функционала Путешествие дня")
@Feature("Покупка тура по карте")
public class PaymentUiTests {
    private static DataHelper.CardData cardData;
    private static TripCardPage tripCard;
    private static TripFormPage tripForm;
    private static List<DataHelperSQL.PaymentEntity> payments;
    private static List<DataHelperSQL.CreditRequestEntity> credits;
    private static List<DataHelperSQL.OrderEntity> orders;

    @BeforeClass
    public void setupClass() {
        DataHelperSQL.setDown();
        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true).savePageSource(true));
    }

    @BeforeMethod
    public void setupMethod() {
        open("http://localhost:8080/");
        tripCard = new TripCardPage();
    }

    @AfterMethod
    public void setDownMethod() {
        DataHelperSQL.setDown();
    }

    @AfterClass
    public void setDownClass() {
        SelenideLogger.removeListener("allure");
    }

    @Story("HappyPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldHappyPath() {
        cardData = DataHelper.getValidApprovedCard();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();

        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(tripCard.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("approved"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Story("SadPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldSadPath() {
        cardData = DataHelper.getValidDeclinedCard();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertBuyOperationWithErrorNotification();

        payments = DataHelperSQL.getPayments();
        credits = DataHelperSQL.getCreditsRequest();
        orders = DataHelperSQL.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(tripCard.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Story("Переключение с формы кредита на форму покупки")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldImmutableInputValuesAfterClickButton() {
        cardData = DataHelper.getValidApprovedCard();

        tripForm = tripCard.clickCreditButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripCard.clickPayButton();
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
    }
    //Валидные значения
    @Story("Имя через дефис и фамилия на латинице")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotification() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateInvalidHolder();
        var matchesHolder = holder;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("Имя и фамилия на латинице состоящие из 30 символов")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotificationWithEn30() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = "Abcdeferererererererererererer Abcdeferererererererererererer";
        var matchesHolder = holder;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

//Невалидные значения
// Поле "Номер карты"

    @Story("15 цифр в поле номера карты")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldUnsuccessfulWith15DigitsInNumber() {
        cardData = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateValidCardNumberWith15Digits();
        var matchesNumber = number;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("number","Неверный формат");
    }

    @Story("13 цифр в поле номера карты")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldUnsuccessfulWith13DigitsInNumber() {
        cardData = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateValidCardNumberWith13Digits();
        var matchesNumber = number;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("number","Неверный формат");
    }

    @Story("Пустое поле номер карты")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyNumber() {
        cardData = DataHelper.getValidApprovedCard();
        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm("", cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue("", cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("number","Поле обязательно для заполнения");
    }

    @Story("Нули в поле номера карты")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithInvalidEmpty() {
        cardData = DataHelper.getValidApprovedCard();
        var number = DataHelper.generateValidCardNumberWith0Digits();
        var matchesNumber = number;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("number","Неверный формат");
    }

    @Story("Заполнение поля номера карты c пробелами вначале и в конце")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldSuccessfulWithStartEndSpacebarInNumber() {
        cardData = DataHelper.getValidApprovedCard();
        var number = " " + cardData.getNumber() + " ";
        var matchesNumber = cardData.getNumber();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(number, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(matchesNumber, cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("number","Неверный формат");
    }

// Поле "Владелец"

    @Story("Кириллица + цифры в поле владелец")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotificationWithCyrillicSymbolInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateInvalidHolderWithCyrillicSymbols45();
        var matchesHolder = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertField("holder","Неверный формат");
    }

    @Story("Кириллица в поле владелец")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotificationWithCyrillicInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateInvalidHolderWithCyrillicSymbols();
        var matchesHolder = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertField("holder","Неверный формат");
    }

    @Story("Пустое поле владелец")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = "";
        var matchesHolder = holder;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertField("holder","Поле обязательно для заполнения");
    }

    @Story("Пробелы вначале и в конце поля владелец")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldAutoDeletingStartEndHyphenInHolder() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = " " + cardData.getHolder() + " ";
        var matchesHolder = cardData.getHolder();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertField("holder","Неверный формат");
    }

    @Story("Латинское имя без фамилии в поле владелец")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotificationWithFirstName() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateInvalidHolderFirstNameEn();
        var matchesHolder = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("Имя на кирилице, фамилия на латинице в поле владелец")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotificationWithFirstRuLastEn() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateInvalidHolderFirstNameRuLastEn();
        var matchesHolder = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertField("holder","Неверный формат");
    }

    @Story("Имя на латинице, фамилия на кирилице в поле владелец")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldVisibleNotificationWithFirstEnLastRu() {
        cardData = DataHelper.getValidApprovedCard();
        var holder = DataHelper.generateInvalidHolderFirstNameEnLastRu();
        var matchesHolder = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), holder, cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), matchesHolder, cardData.getCvc());
        tripForm.assertBuyOperationIsSuccessful();
    }

    // Поле "Месяц"
    @Story("Пустое поле месяц")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = "";
        var matchesMonth = "";

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("month","Поле обязательно для заполнения");
    }

    @Story("Заполнение поля месяц значением 00")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith00InMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = "00";
        var matchesMonth = month;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("month","Неверно указан срок действия карты");
    }

    @Story("Заполнение поля месяц значением 13")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith13InMonth() {
        cardData = DataHelper.getValidApprovedCard();
        var month = "13";
        var matchesMonth = month;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), month, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), matchesMonth, cardData.getYear(), cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("month","Неверно указан срок действия карты");
    }

    // Поле "Год"
    @Story("Пустое поле год")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyYear() {
        cardData = DataHelper.getValidApprovedCard();
        var year = "";
        var matchesYear = year;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), year, cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), matchesYear, cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("year","Поля обязательно для заполнения");
    }

    @Story("Нули в поле год")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithInvalid0SymbolsInYear() {
        cardData = DataHelper.getValidApprovedCard();
        var year = "00";
        var matchesYear = year;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), year, cardData.getHolder(), cardData.getCvc());
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), matchesYear, cardData.getHolder(), cardData.getCvc());
        tripForm.assertField("year","Истёк срок действия карты");
    }

    // CVC/CVV
    @Story("Пустое поле CVC/CVV")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldVisibleNotificationWithEmptyCVC() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = "";
        var matchesCvc = cvc;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertField("cvc","Поле обязательно для заполнения");
    }

    @Story("2 цифры в поле CVC/CVV")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWith2DigitsInCVC() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = DataHelper.generateInvalidCVCWith2Digit();
        var matchesCvc = cvc;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertField("cvc","Неверный формат");
    }

    @Story("4 цифры в поле CVC/CVV")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldSuccessfulWith4DigitsInCVC() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = cardData.getCvc() + DataHelper.generateRandomOneDigit();
        var matchesCvc = cardData.getCvc();

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertBuyOperationIsSuccessful();
    }

    @Story("Нули в поле CVC/CVV")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldVisibleNotificationWithEmptyCVC0() {
        cardData = DataHelper.getValidApprovedCard();
        var cvc = "000";
        var matchesCvc = cvc;

        tripForm = tripCard.clickPayButton();
        tripForm.insertingValueInForm(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), cvc);
        tripForm.matchesByInsertValue(cardData.getNumber(), cardData.getMonth(), cardData.getYear(), cardData.getHolder(), matchesCvc);
        tripForm.assertBuyOperationIsSuccessful();
    }
}