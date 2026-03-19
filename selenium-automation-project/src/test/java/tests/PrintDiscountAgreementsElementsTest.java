package tests;

import driver.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.IotronDashboardPage;
import pages.LoginPage;

import java.util.List;

public class PrintDiscountAgreementsElementsTest {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @Test
    public void printElements() {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("mayur.maradiya", "Nextgen$123");
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForHomePageToLoad();
        homePage.navigateToIotron();
        
        IotronDashboardPage dashboardPage = new IotronDashboardPage(driver);
        try {
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        dashboardPage.navigateToDiscountAgreements();
        
        try {
            Thread.sleep(5000); 
            
            System.out.println("--- INPUTS ---");
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            for (WebElement input : inputs) {
                if (input.isDisplayed()) {
                    System.out.println("ID: " + input.getAttribute("id") + ", Placeholder: " + input.getAttribute("placeholder") + ", Title: " + input.getAttribute("title"));
                }
            }
            
            System.out.println("--- SELECTS ---");
            List<WebElement> selects = driver.findElements(By.tagName("select"));
            for (WebElement select : selects) {
                if (select.isDisplayed()) {
                    System.out.println("ID: " + select.getAttribute("id") + ", Title: " + select.getAttribute("title"));
                }
            }
            
            System.out.println("--- BUTTONS ---");
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            for (WebElement button : buttons) {
                if (button.isDisplayed()) {
                    System.out.println("ID: " + button.getAttribute("id") + ", Title: " + button.getAttribute("title") + ", Text: " + button.getText());
                }
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
