package tests;

import driver.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;

public class ExtractAgreementCaptureHtmlTest {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @Test
    public void extractHtml() {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("mayur.maradiya", "Nextgen$123");
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForHomePageToLoad();
        homePage.navigateToIotron();
        
        try {
            Thread.sleep(5000); 
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // Locate the "Agreement Capture" tree node's content area (which contains the icon even when menu is collapsed)
            WebElement agreementCaptureNode = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//li[.//span[contains(@class, 'a-TreeView-label') and text()='Agreement Capture']]//div[contains(@class, 'a-TreeView-content')]")
            ));
            
            try {
                agreementCaptureNode.click();
            } catch (Exception clickEx) {
                // Fallback to JS click if it's obscured
                ((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", agreementCaptureNode);
            }
            
            // Wait a bit for the animation/ajax
            Thread.sleep(3000);
            
            String pageSource = driver.getPageSource();
            FileWriter writer = new FileWriter("target/agreement_capture_source.html");
            writer.write(pageSource);
            writer.close();
            System.out.println("Agreement Capture HTML saved successfully.");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
