package io.github.sbaumeister.productcrawler.testutils;

import org.openqa.selenium.chrome.ChromeDriver;

public class WebDriverFactory {

    public static ChromeDriver createChromeDriver() {
        ChromeDriver chromeDriver = new ChromeDriver();
        return chromeDriver;
    }
}
