package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractHomeHtmlTest {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @Test
    public void extractHtml() throws Exception {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("mayur.maradiya", "Nextgen$123");
        
        Thread.sleep(10000); // Wait for the home page to load completely after login
        
        String pageSource = driver.getPageSource();
        Path path = Paths.get("target/home_page_source.html");
        Files.writeString(path, pageSource);
        System.out.println("Home Page source written to " + path.toAbsolutePath());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
