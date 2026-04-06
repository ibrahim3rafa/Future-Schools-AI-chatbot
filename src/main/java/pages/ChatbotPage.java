package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChatbotPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private WebElement shadowHost;
    private SearchContext shadowRoot;
    private final WebDriverWait longWait; // separate long wait for bot responses

    public ChatbotPage(WebDriver driver, WebDriverWait wait, SearchContext shadowRoot, WebElement shadowHost) {
        this.driver = driver;
        this.wait = wait;
        this.shadowHost = shadowHost;
        this.shadowRoot = shadowRoot;
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(60)); // bot can be slow
    }
    public String sendQuestion(String question) throws InterruptedException {
        WebElement chatInput = wait.until(d ->
                getShadowRoot().findElement(By.cssSelector("[placeholder*='Type a message...']")));

        // ✅ Count BEFORE typing — input is empty, no ghost div[dir='auto'] yet
        int countBeforeSend = getShadowRoot()
                .findElements(By.cssSelector("div[dir='auto']"))
                .size();

        chatInput.click();
        Thread.sleep(500); // small pause to ensure input is focused and ready
        chatInput.clear();
        chatInput.sendKeys(question);

        WebElement sendBtn = getShadowRoot().findElement(By.cssSelector("[title*='Send message']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sendBtn);

        return String.valueOf(countBeforeSend);
    }
    public String getBotAnswer(String baselineCountStr) {
        By messagesLocator = By.cssSelector("div[dir='auto']");
        int baselineCount = Integer.parseInt(baselineCountStr);

        // From debug: baseline=1, user added → count=2, bot added → count=3
        // Bot reply is always at index (baselineCount + 2 - 1) = baselineCount + 1 in 0-based
        // OR: wait until size >= baselineCount+2, then read index baselineCount+1
        int expectedTotalCount = baselineCount + 2;  // +1 user, +1 bot
        int botIndex = baselineCount + 1;            // 0-based index of the bot reply

        AtomicReference<String> lastSeenText = new AtomicReference<>(null);
        AtomicInteger stableCount = new AtomicInteger(0);
        int REQUIRED_STABLE_POLLS = 3; // 3 × 500ms = 1.5s stability

        return longWait.until(driver -> {
            try {
                List<WebElement> messages = getShadowRoot().findElements(messagesLocator);

                // Wait until bot reply node exists
                if (messages.size() < expectedTotalCount) {
                    stableCount.set(0);
                    lastSeenText.set(null);
                    return null;
                }

                String currentText = messages.get(botIndex).getText();

                if (currentText.trim().isEmpty()) {
                    stableCount.set(0);
                    lastSeenText.set(null);
                    return null;
                }

                // Stability: text must stop changing (streaming done)
                if (currentText.equals(lastSeenText.get())) {
                    if (stableCount.incrementAndGet() >= REQUIRED_STABLE_POLLS) {
                        return currentText;
                    }
                } else {
                    lastSeenText.set(currentText);
                    stableCount.set(0);
                }

                return null;

            } catch (StaleElementReferenceException e) {
                stableCount.set(0);
                return null;
            }
        });
    }
    private SearchContext getShadowRoot() {
        WebElement host = driver.findElement(By.cssSelector("#e-chat"));
        return (SearchContext) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].shadowRoot", host);
    }
}