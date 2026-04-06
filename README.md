# 🤖 Future Schools AI Chatbot — Test Automation Framework

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java" />
  <img src="https://img.shields.io/badge/Selenium-4.17.0-green?style=for-the-badge&logo=selenium" />
  <img src="https://img.shields.io/badge/TestNG-7.9.0-red?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Apache_POI-5.2.5-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge" />
</p>

> An end-to-end automated testing framework for the AI chatbot embedded in the **Future International & Language Schools** website ([1122.tel](https://www.1122.tel/)). The framework sends real questions to the chatbot, captures streaming responses reliably, and writes results back to Excel — all without a single `Thread.sleep()`.

---

## 👤 Author

**Ibrahim Arafa**  
Software Test Engineer  
📧 Built with passion for quality automation

---

## 🏫 About the Target System

**Future International & Language Schools** is an Egyptian private school group operating two branches:

| Branch | Location |
|--------|----------|
| 🏫 Al-Sadat Branch | Sadat City, Menofia Governorate |
| 🏫 Menouf Branch | Menouf City, Menofia Governorate |

The school offers:
- **International Division** — American Diploma, COGNIA-accredited
- **Semi-International (Language) Division** — Egyptian curriculum with international enhancements

The chatbot (powered by [24 Smart Technologies](https://www.1122.tel/)) lives inside a **Shadow DOM** widget and handles parent inquiries about admissions, tuition, schedules, and more.

---

## 🎯 Project Goals

- Automate question-answer interaction with the school's AI chatbot
- Validate chatbot responses against expected answers
- Generate a test report in Excel with Q&A pairs for review
- Handle streaming bot responses reliably without brittle sleeps

---

## 🏗️ Project Structure

```
Future/
├── src/
│   ├── main/java/
│   │   └── pages/
│   │       └── ChatbotPage.java        # Page Object for chatbot interactions
│   └── test/java/
│       ├── tests/
│       │   └── ChatbotExcelTest.java   # Data-driven TestNG test
│       └── utils/
│           └── ExcelUtil.java          # Apache POI Excel read/write utility
├── src/test/resources/
│   └── chatbot_data.xlsx               # Input questions + output answers
├── pom.xml
└── README.md
```

---

## ⚙️ Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 (MS Build) | Core language |
| Selenium WebDriver | 4.17.0 | Browser automation |
| TestNG | 7.9.0 | Test framework & execution |
| Apache POI | 5.2.5 | Excel read/write (`.xlsx`) |
| ChromeDriver | Auto-managed | Chrome browser driver |
| IntelliJ IDEA | 2024.3.x | IDE |

---

## 🔑 Key Technical Challenges & Solutions

### 1. Shadow DOM Penetration
The chatbot widget is rendered inside a **Shadow DOM**, making standard Selenium locators fail.

```java
// Solution: JavaScript-based shadow root extraction
private SearchContext getShadowRoot() {
    WebElement host = driver.findElement(By.cssSelector("#e-chat"));
    return (SearchContext) ((JavascriptExecutor) driver)
            .executeScript("return arguments[0].shadowRoot", host);
}
```

### 2. Streaming Response Detection (No `Thread.sleep`)
The bot streams its answer token by token. The framework uses a **stability counter pattern** — polling until the text stops changing for 3 consecutive polls (≈1.5s).

```
Poll N:   "السلام عليكم! 👋 للإجابة..."     → text changed, counter reset to 0
Poll N+1: "السلام عليكم! 👋 للإجابة على..."  → text changed, counter reset to 0
Poll N+2: "السلام عليكم! 👋 [full answer]"   → same, counter = 1
Poll N+3: "السلام عليكم! 👋 [full answer]"   → same, counter = 2
Poll N+4: "السلام عليكم! 👋 [full answer]"   → same, counter = 3 ✅ RETURN
```

### 3. Precise Message Targeting (Correct Index Every Time)
Both user messages and bot replies use `div[dir='auto']`. The framework snapshots the **count before typing** (not before sending — to avoid picking up the input field's own render).

```java
// Count BEFORE typing — cleanest baseline
int countBeforeSend = getShadowRoot()
        .findElements(By.cssSelector("div[dir='auto']"))
        .size();
// ...sendKeys + click...
// Bot reply will be at index: countBeforeSend + 1 (0-based)
// Total elements expected:     countBeforeSend + 2
```

### 4. StaleElementReferenceException Handling
The React-based chatbot re-renders DOM nodes while streaming. Every element access is wrapped to retry gracefully:

```java
catch (StaleElementReferenceException e) {
    stableCount.set(0);
    return null; // Triggers WebDriverWait retry
}
```

---

## 📊 Data-Driven Testing with Excel

Questions are read from `chatbot_data.xlsx` and answers are written back after each test:

| Column A (Input) | Column B (Output) |
|-----------------|------------------|
| امتحانات الترم الأول إمتى؟ | السلام عليكم! 👋 للإجابة... |
| كام مصروفات Grade 10؟ | السلام عليكم! 👋 للإجابة... |
| ... | ... |

---

## 🚀 How to Run

### Prerequisites
- Java 21+
- Maven 3.8+
- Google Chrome (latest)

### Steps

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd Future

# 2. Add your Excel file to:
src/test/resources/chatbot_data.xlsx

# 3. Run tests
mvn test

# OR run directly from IntelliJ IDEA using the TestNG runner
```

### Excel File Format
Column A should contain questions (one per row, starting from row 1). Column B will be auto-populated with the bot's answers after the test run.

---

## 🧠 Architecture — How It All Works

```
ChatbotExcelTest.java
        │
        ├── @BeforeClass setup()
        │       ├── Launch ChromeDriver
        │       ├── Navigate to https://www.1122.tel/
        │       ├── Locate Shadow Host (#e-chat)
        │       ├── Extract Shadow Root via JS
        │       └── Click Logo to open chat widget
        │
        └── @Test chatbotExcelDataDrivenTest()
                └── for each row in Excel:
                        ├── chatbot.sendQuestion(question)
                        │       ├── Find input in Shadow DOM
                        │       ├── Snapshot div[dir='auto'] count
                        │       ├── Type question
                        │       └── JS click Send button
                        │
                        └── chatbot.getBotAnswer(baseline)
                                ├── Wait for total count = baseline + 2
                                ├── Read text at index baseline + 1
                                ├── Poll until text stable for 3 cycles
                                └── Return final answer
```

---

## 📋 Sample Test Output (Console)

```
Question1  : امتحانات الترم الأول في القسم الدولي إمتى؟
Answer     : السلام عليكم! 👋 للإجابة على سؤالك بشكل أفضل، أولاً حدد أي فرع تهتم به...

Question7  : مدرسة المستقبل في السادات دي نفس فيوتشر؟
Answer     : نعم، مدرسة فيوتشر الدولية للغات - فرع السادات هي نفسها مدرسة المستقبل...

Question10 : المدرسة فيها نظام IGCSE؟
Answer     : لا، مدرسة فيوتشر الدولية واللغات لا تقدم نظام IGCSE حاليًا...
```

---

## ⚠️ Known Warnings (Non-Critical)

These appear in the console but do **not** affect test execution:

```
WARNING: Unable to find CDP implementation matching 146
→ Chrome 146 is newer than Selenium 4.17.0's bundled CDP.
  Fix: upgrade to selenium-java 4.23+ or add selenium-devtools-v146 dependency.

ERROR: Log4j2 could not find a logging implementation
→ No log4j-core on classpath. Add it to pom.xml if logging is needed.
```

---

## 🔮 Potential Improvements

- [ ] Add expected answers column to Excel for assertion/comparison
- [ ] Generate HTML test report (Extent Reports or Allure)
- [ ] Add screenshot capture on failure
- [ ] Parameterize browser via config file (Chrome, Edge, Firefox)
- [ ] Add CI/CD pipeline (GitHub Actions)
- [ ] Upgrade Selenium to 4.23+ to resolve CDP version warning
- [ ] Support multi-language question sets

---

## 📄 License

This project is for internal QA and academic purposes.  
All rights reserved © Ibrahim Arafa, 2026.