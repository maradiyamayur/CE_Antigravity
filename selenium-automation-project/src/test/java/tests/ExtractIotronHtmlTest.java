package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;

import java.io.FileWriter;
import java.io.IOException;

public class ExtractIotronHtmlTest {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @Test
    public void extractIotronHtml() {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("mayur.maradiya", "Nextgen$123");
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForHomePageToLoad();
        homePage.navigateToIotron();
        
        try {
            // Wait for IOTRON application to load properly
            Thread.sleep(10000); 
            
            String pageSource = driver.getPageSource();
            FileWriter writer = new FileWriter("target/iotron_page_source.html");
            writer.write(pageSource);
            writer.close();
            System.out.println("IOTRON page HTML saved successfully.");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
