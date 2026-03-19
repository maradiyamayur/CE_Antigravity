package tests;

import driver.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import pages.ForecastReportPage;
import utils.CsvUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.time.Duration;

public class ExtractForecastMenuHtmlTest {
    private WebDriver driver;
    private List<Map<String, String>> downloadedReportData;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @Test
    public void extractForecastMenuHtml() {
        driver.get("https://fuat1.dev.nextgen.local/fuat/");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("mayur.maradiya", "Nextgen$123");
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForHomePageToLoad();
        homePage.navigateToIotron();
        
        IotronDashboardPage dashboardPage = new IotronDashboardPage(driver);
        try {
            // Use the updated navigation method
            dashboardPage.navigateToForecastReportPerAgreement();
            
            // Use the new ForecastReportPage object
            ForecastReportPage forecastPage = new ForecastReportPage(driver);
            
            System.out.println("Selecting 'All' for Show Agreements...");
            forecastPage.selectAllAgreements();
            
            String searchAgreement = "Transatel 2025 - 2030";
            System.out.println("Selecting agreement: " + searchAgreement);
            forecastPage.selectAgreement(searchAgreement);
            
            System.out.println("Refreshing report...");
            forecastPage.clickRefreshReport();
            
            // Click IOT Calculation Report button as requested
            forecastPage.clickIotCalculationReport();
            
            // Click Refresh button again as requested (this is on the detail page now)
            System.out.println("Clicking Detail Refresh button...");
            forecastPage.clickDetailRefresh();
            
            // Wait for the target page to load/stabilize
            Thread.sleep(5000); 
            
            System.out.println("Wait 3s for Actions menu to be ready...");
            Thread.sleep(3000);
            
            System.out.println("Starting download flow...");
            forecastPage.clickActionsMenu();
            forecastPage.selectDownloadFromActions();
            forecastPage.selectCsvOptionInPopup();
            forecastPage.clickDownloadInPopup();
            
            System.out.println("Download flow completed.");
            
            // Handle CSV reading
            String downloadDir = System.getProperty("user.dir") + File.separator + "target" + File.separator + "downloads";
            File latestFile = waitForFileDownload(downloadDir, "IOT_Calculation_Detail", 30);
            
            if (latestFile != null) {
                System.out.println("Downloaded file found: " + latestFile.getAbsolutePath());
                downloadedReportData = CsvUtils.readCsv(latestFile.getAbsolutePath());
                System.out.println("Successfully read " + downloadedReportData.size() + " records from report into memory.");
                
                // Print sample data
                if (!downloadedReportData.isEmpty()) {
                    System.out.println("Sample record: " + downloadedReportData.get(0));
                }
            } else {
                System.out.println("Warning: Downloaded report file not found within timeout.");
            }
            
            String pageSource = driver.getPageSource();
            FileWriter writer = new FileWriter("target/forecast_report_page_with_agreement.html");
            writer.write(pageSource);
            writer.close();
            System.out.println("Forecast Report page with selected agreement HTML saved successfully.");
            
        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
            try {
                String errorPageSource = driver.getPageSource();
                FileWriter writer = new FileWriter("target/forecast_report_error_page.html");
                writer.write(errorPageSource);
                writer.close();
                System.out.println("Error page source saved to target/forecast_report_error_page.html");
            } catch (IOException ioException) {
                System.out.println("Could not save error page source: " + ioException.getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Test failed: " + e.getMessage(), e);
        }
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private File waitForFileDownload(String downloadDir, String fileNamePrefix, int timeoutSeconds) {
        System.out.println("Waiting for file starting with '" + fileNamePrefix + "' in " + downloadDir + "...");
        File dir = new File(downloadDir);
        for (int i = 0; i < timeoutSeconds; i++) {
            File[] files = dir.listFiles((d, name) -> name.startsWith(fileNamePrefix) && !name.endsWith(".crdownload") && !name.endsWith(".tmp"));
            if (files != null && files.length > 0) {
                // Return the most recently modified file among matches
                File latest = files[0];
                for (File f : files) {
                    if (f.lastModified() > latest.lastModified()) {
                        latest = f;
                    }
                }
                return latest;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @AfterMethod
    public void tearDown() {
        // DriverFactory.quitDriver(); // Commented out to keep browser open
    }
}
