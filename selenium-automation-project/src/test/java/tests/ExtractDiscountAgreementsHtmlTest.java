package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.IotronDashboardPage;
import pages.LoginPage;

import java.io.FileWriter;
import java.io.IOException;

public class ExtractDiscountAgreementsHtmlTest {
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
        
        // Step 3: Navigate to Discount Agreements
        IotronDashboardPage dashboardPage = new IotronDashboardPage(driver);
        try {
            Thread.sleep(5000); // Wait for APEX application to load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        dashboardPage.navigateToDiscountAgreements();
        
        try {
            Thread.sleep(5000); // Wait for the new page to load
            
            String pageSource = driver.getPageSource();
            FileWriter writer = new FileWriter("target/discount_agreements_source.html");
            writer.write(pageSource);
            writer.close();
            System.out.println("Discount Agreements HTML saved successfully.");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
