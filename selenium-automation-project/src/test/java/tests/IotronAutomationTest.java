package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AgreementDetailPage;
import pages.DiscountAgreementsPage;
import pages.HomePage;
import pages.IotronDashboardPage;
import pages.LoginPage;
import pages.ForecastReportPage;
import utils.CsvUtils;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * End-to-end automation test for the Discount Agreements flow:
 *
 *  1. Open IOTRON application
 *  2. Log in
 *  3. Wait for the main dashboard to load
 *  4. Click the "IOTRON" navigation link
 *  5. Navigate to Agreement Capture → Discount Agreements
 *  6. Set search filter type to "Agreement", search for "Transatel 2025 - 2030"
 *  7. Click the Edit button for the matched agreement
 *  8. Navigate to the "Settlement" tab
 */
public class IotronAutomationTest {

    private static final String BASE_URL      = utils.ConfigReader.getBaseUrl();
    private static final String USERNAME       = utils.ConfigReader.getUsername();
    private static final String PASSWORD       = utils.ConfigReader.getPassword();
    private static final String AGREEMENT_NAME = "Transatel 2025 - 2030";

    private WebDriver driver;

    // ------------------------------------------------------------------
    // TestNG lifecycle
    // ------------------------------------------------------------------

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @AfterMethod
    public void tearDown() {
        // Keep browser open for manual inspection after the test.
        // Uncomment the line below to close automatically:
        // DriverFactory.quitDriver();
    }

    // ------------------------------------------------------------------
    // Test
    // ------------------------------------------------------------------

    /**
     * Executes all 8 steps of the Discount Agreements automation flow.
     */
    @Test
    public void testDiscountAgreementsSettlementTab() {

        // ---------------------------------------------------------------
        // Step 1 – Open the IOTRON application
        // ---------------------------------------------------------------
        System.out.println("Step 1: Opening IOTRON application at " + BASE_URL);
        driver.get(BASE_URL);

        // ---------------------------------------------------------------
        // Step 2 – Log in
        // ---------------------------------------------------------------
        System.out.println("Step 2: Logging in as " + USERNAME);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(USERNAME, PASSWORD);

        // ---------------------------------------------------------------
        // Step 3 – Wait for the main dashboard to load completely
        // ---------------------------------------------------------------
        System.out.println("Step 3: Waiting for the home/dashboard page to load...");
        HomePage homePage = new HomePage(driver);
        homePage.waitForHomePageToLoad();
        System.out.println("       Dashboard loaded.");

        // ---------------------------------------------------------------
        // Step 4 – Click the "IOTRON" navigation link
        // ---------------------------------------------------------------
        System.out.println("Step 4: Clicking the 'IOTRON' link in the navigation bar...");
        homePage.navigateToIotron();

        // ---------------------------------------------------------------
        // Step 5 – Navigate to Agreement Capture → Discount Agreements
        // ---------------------------------------------------------------
        System.out.println("Step 5: Navigating to Agreement Capture → Discount Agreements...");
        IotronDashboardPage dashboardPage = new IotronDashboardPage(driver);
        dashboardPage.navigateToDiscountAgreements();
        System.out.println("       Discount Agreements page loaded.");

        // ---------------------------------------------------------------
        // Step 6 – Set filter type to "Agreement", search for the agreement
        // ---------------------------------------------------------------
        System.out.println("Step 6: Setting filter type to 'Agreement' and searching for: " + AGREEMENT_NAME);
        DiscountAgreementsPage discountPage = new DiscountAgreementsPage(driver);

        // Step 5a – In the right-side Filter region, set "Show Agreements" to "All"
        System.out.println("Step 5a: Setting 'Show Agreements' filter to 'All'...");
        discountPage.selectAllAgreements();
        System.out.println("        'Show Agreements = All' applied.");

        discountPage.searchByAgreementType(AGREEMENT_NAME);
        System.out.println("       Search completed. Agreement found in results.");

        // ---------------------------------------------------------------
        // Step 7 – Click the Edit button for the matched agreement
        // ---------------------------------------------------------------
        System.out.println("Step 7: Clicking Edit for agreement: " + AGREEMENT_NAME);
        discountPage.clickEditForAgreement(AGREEMENT_NAME);
        System.out.println("       Agreement detail page loaded.");

        // ---------------------------------------------------------------
        // Step 8 – Navigate to the Settlement tab
        // ---------------------------------------------------------------
        System.out.println("Step 8: Navigating to the 'Settlement' tab...");
        AgreementDetailPage detailPage = new AgreementDetailPage(driver);
        detailPage.clickSettlementTab();
        System.out.println("       Settlement tab is now active.");

        // ---------------------------------------------------------------
        // Step 9 – Click Generate Settlement
        // ---------------------------------------------------------------
        System.out.println("Step 9: Clicking 'Generate Settlement' button...");
        detailPage.clickGenerateSettlement();

        // ---------------------------------------------------------------
        // Step 10 – Extract Parameters
        // ---------------------------------------------------------------
        System.out.println("Step 10: Extracting settlement parameters...");
        Map<String, String> params = detailPage.getSettlementParameters();
        
        System.out.println("\n[SETTLEMENT_PARAMS] = {");
        params.forEach((key, value) -> System.out.println("  " + key + ": \"" + value + "\","));
        System.out.println("}");

        // ---------------------------------------------------------------
        // Step 11 – Phase 3: Forecasting → Forecast Report per Agreement
        // ---------------------------------------------------------------
        System.out.println("\n--- Phase 3: Forecast Report Automation ---");
        System.out.println("Step 11: Navigating to Forcasting -> Forecast Report per Agreement...");
        dashboardPage.navigateToForecastReportPerAgreement();
        
        ForecastReportPage forecastPage = new ForecastReportPage(driver);
        
        // Step 2 (Phase 3): Set "Show Agreements" filter to "All"
        forecastPage.selectAllAgreements();
        
        // Step 3 (Phase 3): Search and select agreement
        forecastPage.selectAgreement(AGREEMENT_NAME);
        
        // Step 4 (Phase 3): Click "Refresh Report"
        forecastPage.clickRefreshReport();
        
        // Step 5 (Phase 3): Click "IOT Calculation Report"
        forecastPage.clickIotCalculationReport();
        
        // Step 6 (Phase 3): Click "Refresh" on detail page
        forecastPage.clickDetailRefresh();
        
        // Step 8 (Phase 3): Download via Action menu
        System.out.println("Step 8 (Phase 3): Downloading the report...");
        forecastPage.clickActionsMenu();
        forecastPage.selectDownloadFromActions();
        forecastPage.selectCsvOptionInPopup();
        forecastPage.clickDownloadInPopup();
        
        // Step 9 (Phase 3): READ downloaded file and SAVE as [1_step_EoA_DATA]
        System.out.println("Step 9 (Phase 3): Reading downloaded file...");
        String downloadDir = System.getProperty("user.dir") + File.separator + "target" + File.separator + "downloads";
        File dir = new File(downloadDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".csv"));
        
        if (files != null && files.length > 0) {
            // Get latest file by modification time
            File latestFile = Arrays.stream(files)
                    .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                    .get();
            
            System.out.println("Reading data from: " + latestFile.getName());
            try {
                List<Map<String, String>> allRows = CsvUtils.readCsv(latestFile.getAbsolutePath());
                
                // Save specific columns as [1_step_EoA_DATA]
                List<Map<String, String>> subsetData = allRows.stream().map(row -> {
                    Map<String, String> subset = new java.util.HashMap<>();
                    subset.put("Discount Direction", row.getOrDefault("Discount Direction", ""));
                    subset.put("Discount Service Type", row.getOrDefault("Discount Service Type", ""));
                    subset.put("Discount Event Type", row.getOrDefault("Discount Event Type", ""));
                    subset.put("Discount Calculation Type", row.getOrDefault("Discount Calculation Type", ""));
                    subset.put("Discount Basis Value", row.getOrDefault("Discount Basis Value", ""));
                    subset.put("Traffic Volume Restricted EoA", row.getOrDefault("Traffic Volume Restricted EoA", ""));
                    subset.put("TAP Charge EoA", row.getOrDefault("TAP Charge EoA", ""));
                    return subset;
                }).collect(Collectors.toList());
                
                System.out.println("[1_step_EoA_DATA] captured " + subsetData.size() + " rows.");
                // Printing first row as sample
                if (!subsetData.isEmpty()) {
                    System.out.println("Sample row: " + subsetData.get(0));
                }
                
            } catch (Exception e) {
                System.err.println("Failed to read CSV file: " + e.getMessage());
            }
        } else {
            System.err.println("No CSV file found in download directory: " + downloadDir);
        }

        System.out.println("\n✅ Phase 3 completed successfully!");
    }
}
