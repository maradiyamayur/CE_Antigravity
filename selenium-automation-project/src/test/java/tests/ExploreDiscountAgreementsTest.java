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
import pages.IotronDashboardPage;
import pages.LoginPage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;

public class ExploreDiscountAgreementsTest {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @Test
    public void explore() {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("mayur.maradiya", "Nextgen$123");
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForHomePageToLoad();
        homePage.navigateToIotron();
        
        IotronDashboardPage dashboardPage = new IotronDashboardPage(driver);
        dashboardPage.navigateToDiscountAgreements();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        try {
            Thread.sleep(5000); 
            
            // 1. Click 'Go' to see default results
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("p301ir1_search_button")));
            searchButton.click();
            
            Thread.sleep(5000);
            
            saveHtml("target/all_agreements_source.html");
            
            // 2. Open Column Selector
            WebElement columnSelector = driver.findElement(By.id("p301ir1_column_search_root"));
            columnSelector.click();
            Thread.sleep(2000);
            saveHtml("target/column_selector_source.html");
            
            // 3. Try to search for "Transatel" specifically
            WebElement searchField = driver.findElement(By.id("p301ir1_search_field"));
            searchField.clear();
            searchField.sendKeys("Transatel");
            searchButton.click();
            
            Thread.sleep(5000);
            saveHtml("target/transatel_search_source.html");
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void saveHtml(String fileName) {
        try {
            String source = driver.getPageSource();
            FileWriter writer = new FileWriter(fileName);
            writer.write(source);
            writer.close();
            System.out.println("Saved: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
