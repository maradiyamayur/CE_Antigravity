package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractHtmlTest {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @Test
    public void extractHtml() throws Exception {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        Thread.sleep(5000); // Wait for page to fully load and redirect
        String pageSource = driver.getPageSource();
        Path path = Paths.get("target/page_source.html");
        Files.writeString(path, pageSource);
        System.out.println("Page source written to " + path.toAbsolutePath());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
