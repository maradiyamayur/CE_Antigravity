package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BasePage;
import pages.LoginPage;

public class SampleTest {
    private WebDriver driver;
    private BasePage basePage;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
        basePage = new BasePage(driver);
    }

    @Test
    public void testPageTitle() {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        String title = basePage.getPageTitle();
        // Adjust the expected title later based on what the page actually returns
        Assert.assertNotNull(title, "Page title should not be null!");
    }

    @Test
    public void testLogin() {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        LoginPage loginPage = new LoginPage(driver);
        
        loginPage.login("mayur.maradiya", "Nextgen$123");
        
        // Add an assertion here after login to verify success.
        // For example, waiting for a dashboard element or checking the URL
        // Assert.assertTrue(driver.getCurrentUrl().contains("dashboard"));
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
