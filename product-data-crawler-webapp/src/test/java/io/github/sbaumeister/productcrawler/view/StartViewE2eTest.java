package io.github.sbaumeister.productcrawler.view;

import io.github.sbaumeister.productcrawler.testutils.WebDriverFactory;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.List;

import static io.github.sbaumeister.productcrawler.testutils.TestTags.E2E;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;

@Tag(E2E)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class StartViewE2eTest {

    static ChromeDriver chromeDriver;

    @LocalServerPort
    int randomPort;
    String baseUrl;

    @BeforeAll
    static void beforeClass() {
        chromeDriver = WebDriverFactory.createChromeDriver();
    }

    @AfterAll
    static void afterClass() {
        chromeDriver.quit();
    }

    @BeforeEach
    void beforeTest() {
        baseUrl = String.format("http://localhost:%s", randomPort);
    }

    @Test
    @DisplayName("Verify that product list contains at least one item after GTIN search")
    void testSearchBehaviour() throws InterruptedException {
        chromeDriver.navigate().to(baseUrl);
        WebDriverWait wait = new WebDriverWait(chromeDriver, 10);
        WebElement input = chromeDriver.findElement(By.id("search-input"));

        input.sendKeys("5053990123742");
        input.sendKeys(Keys.RETURN);

        List<WebElement> productItems = wait.until(presenceOfAllElementsLocatedBy(By.className("search-result-item")));
        assertTrue(productItems.size() > 0);
    }
}