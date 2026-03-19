package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.*;

import java.io.FileWriter;
import java.io.IOException;

public class ExtractDiscountDetailHtmlTest {
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
        
        IotronDashboardPage dashboardPage = new IotronDashboardPage(driver);
        dashboardPage.navigateToDiscountAgreements();
        
        DiscountAgreementsPage discountPage = new DiscountAgreementsPage(driver);
        try {
            saveHtml(driver, "target/1_init_discount_agreements.html");
            discountPage.selectAllAgreements();
            saveHtml(driver, "target/2_after_filter_all.html");
            
            String searchString = "Transatel";
            discountPage.searchAgreement(searchString);
            saveHtml(driver, "target/3_search_results_source.html");
            
            discountPage.clickEditForAgreement(searchString);
            Thread.sleep(8000); // Wait for detail page
            saveHtml(driver, "target/4_agreement_detail_source.html");
            
            AgreementDetailPage detailPage = new AgreementDetailPage(driver);
            detailPage.clickSettlementTab();
            Thread.sleep(3000); // Wait for settlement content to load/stabilize
            saveHtml(driver, "target/5_settlement_tab_source.html");
            
        } catch (Exception e) {
            System.err.println("Extraction failed: " + e.getMessage());
            try { saveHtml(driver, "target/failure_state_source.html"); } catch (IOException ignore) {}
            e.printStackTrace();
        }
    }

    private void saveHtml(WebDriver driver, String fileName) throws IOException {
        String pageSource = driver.getPageSource();
        java.nio.file.Files.writeString(java.nio.file.Paths.get(fileName), pageSource);
        System.out.println("Saved HTML to: " + fileName);
    }
    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
