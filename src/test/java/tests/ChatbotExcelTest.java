package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.ChatbotPage;
import utils.ExcelUtil;

import java.time.Duration;

public class ChatbotExcelTest {

    public WebDriver driver;
    public WebDriverWait wait;
    private ChatbotPage chatbot;
    private ExcelUtil excel;
    private WebElement shadowHost;
    private SearchContext shadowRoot;

    @BeforeClass
    public void setup() throws Exception {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
        driver.get("https://www.1122.tel/");

        // wait for shadow host
        shadowHost = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("e-chat"))
        );

        // get shadow root
        shadowRoot = shadowHost.getShadowRoot();

        // wait for chat button
        WebElement chatButton = wait.until(d ->
                shadowRoot.findElement(By.cssSelector("img[alt='Logo']"))
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chatButton);

//        // wait for disclaimer accept button
//        WebElement startChat = wait.until(d ->
//                shadowRoot.findElement(By.cssSelector("#disclaimer-accept"))
//        );
//
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", startChat);
//
        chatbot = new ChatbotPage(driver, wait, shadowRoot,shadowHost);
        excel = new ExcelUtil("chatbot_data.xlsx");

    }

    @Test
    public void chatbotExcelDataDrivenTest() throws Exception {
        for (int i = 1; i <= excel.getRowCount(); i++) {
            String question = excel.getQuestion(i);
            System.out.println("Question" + i + " : " + question);
            String baseline = chatbot.sendQuestion(question);   // now returns String
            String answer = chatbot.getBotAnswer(baseline);     // pass baseline
            System.out.println("Answer: " + answer);
            excel.writeAnswer(i, answer);
        }
    }

    @AfterClass
    public void tearDown() {
//        driver.quit();
    }
}
