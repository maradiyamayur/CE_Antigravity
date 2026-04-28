package tests;

import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AgreementDetailPage;
import pages.DiscountAgreementsPage;
import pages.HomePage;
import pages.IotronDashboardPage;
import pages.LoginPage;
import pages.ForecastReportPage;
import utils.AgreementConfig;
import utils.CsvUtils;
import utils.FileDownloadUtils;
import utils.XlsxUtils;
import utils.SettlementDataStore;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * End-to-end automation test for the Discount Agreements flow:
 *
 * 1. Open IOTRON application
 * 2. Log in
 * 3. Wait for the main dashboard to load
 * 4. Click the "IOTRON" navigation link
 * 5. Navigate to Agreement Capture → Discount Agreements
 * 6. Set search filter type to "Agreement", search for "Transatel 2025 - 2030"
 * 7. Click the Edit button for the matched agreement
 * 8. Navigate to the "Settlement" tab
 */
public class IotronAutomationTest {

    private static final String BASE_URL = utils.ConfigReader.getBaseUrl();
    private static final String USERNAME = utils.ConfigReader.getUsername();
    private static final String PASSWORD = utils.ConfigReader.getPassword();
    // AGREEMENT_NAME is now driven dynamically from agreements.json (see
    // AgreementConfig).

    /** Central download directory – used for cleanup and CSV reading. */
    public static final String DOWNLOAD_DIR = System.getProperty("user.dir") + File.separator + "target"
            + File.separator + "downloads";

    private WebDriver driver;

    // ------------------------------------------------------------------
    // TestNG lifecycle
    // ------------------------------------------------------------------

    @BeforeClass
    public void cleanupDownloads() {
        System.out.println("[Setup] Cleaning download directory before test run...");
        FileDownloadUtils.cleanDownloadDirectory(DOWNLOAD_DIR);
    }

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
        // Dynamic Agreement Loop – driven by agreements.json
        // Steps 6–11 execute once per enabled agreement.
        // Steps 1–5 (login + dashboard navigation) run only once above.
        // ---------------------------------------------------------------
        List<String> enabledAgreements;
        try {
            enabledAgreements = AgreementConfig.loadEnabledAgreements();
        } catch (Exception e) {
            System.err.println("[AgreementConfig] Failed to load agreements.json: " + e.getMessage());
            throw new RuntimeException(e);
        }

        if (enabledAgreements.isEmpty()) {
            System.out.println("[AgreementConfig] No enabled agreements found. Skipping Steps 6–11.");
        }

        int iterationIndex = 0;
        for (String AGREEMENT_NAME : enabledAgreements) {
            System.out.println("\n========================================");
            System.out.println("Processing Agreement: " + AGREEMENT_NAME);
            System.out.println("========================================");

            // ---------------------------------------------------------------
            // Reset – Two-step Action-menu reset at the START of every
            // iteration (except the very first one, where the page is already
            // in its initial state).
            // ---------------------------------------------------------------
            DiscountAgreementsPage discountPage = new DiscountAgreementsPage(driver);
            if (iterationIndex > 0) {
                System.out.println("[Loop] Iteration " + iterationIndex + ": Navigating back to Discount Agreements...");
                dashboardPage.navigateToDiscountAgreements();

                // Step 1 – Click "Reset" from the Actions menu
                // Step 2 – Click "Apply" on the confirmation popup
                System.out.println("[Loop] Running two-step Action-menu reset to clear search field...");
                discountPage.resetViaActionMenu();
                System.out.println("[Loop] Search field cleared via Action-menu reset.");
            }
            iterationIndex++;

            // ---------------------------------------------------------------
            // Step 6 – Set filter type to "Agreement", search for the agreement
            // ---------------------------------------------------------------
            System.out.println("Step 6: Setting filter type to 'Agreement' and searching for: " + AGREEMENT_NAME);

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
            // Declare targetDir once — reused in Step 9a and Step 7 Phase 3 (Bug #5 fix)
            String targetDir = System.getProperty("user.dir") + File.separator + "target";

            // Bug #3 fix: snapshot existing XLSX files BEFORE the download starts
            java.util.Set<String> preXlsxNames = new java.util.HashSet<>();
            File xlsxDirRef = new File(DOWNLOAD_DIR);
            {
                File[] preXlsx = xlsxDirRef.listFiles((d, name) -> name.toLowerCase().endsWith(".xlsx"));
                if (preXlsx != null)
                    for (File f : preXlsx)
                        preXlsxNames.add(f.getName());
            }
            System.out.println(
                    "[Snapshot] " + preXlsxNames.size() + " existing XLSX file(s) before Generate Settlement.");

            System.out.println("Step 9: Clicking 'Generate Settlement' button...");
            detailPage.clickGenerateSettlement();

            // ---------------------------------------------------------------
            // Step 9a – Detect & persist the downloaded Settlement XLSX
            // (purely additive – no existing steps modified)
            // ---------------------------------------------------------------
            // Bug #3 fix: wait up to 30 s for a NEW XLSX that was not in the pre-snapshot
            System.out.println("Step 9a: Waiting for NEW Settlement XLSX to download...");
            File latestXlsx = null;
            long xlsxDeadline = System.currentTimeMillis() + 30_000;
            while (System.currentTimeMillis() < xlsxDeadline && latestXlsx == null) {
                File[] current = xlsxDirRef.listFiles((d, name) -> name.toLowerCase().endsWith(".xlsx"));
                if (current != null) {
                    for (File f : current) {
                        if (!preXlsxNames.contains(f.getName())) {
                            latestXlsx = f;
                            break;
                        }
                    }
                }
                if (latestXlsx == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (latestXlsx != null) {
                System.out.println("Step 9a: Found NEW Settlement XLSX: " + latestXlsx.getName());

                // Print available sheet names for easy diagnostics / troubleshooting
                List<String> sheetNames = XlsxUtils.listSheetNames(latestXlsx.getAbsolutePath());
                System.out.println("Step 9a: Sheets found in file: " + sheetNames);

                // ── Sheet 1: Settlement Summary ──────────────────────────────
                // Try by name (case-insensitive), fall back to index 0
                List<Map<String, String>> summaryRows;
                String summarySheetName = sheetNames.stream()
                        .filter(s -> s.equalsIgnoreCase("Settlement Summary"))
                        .findFirst().orElse(null);
                if (summarySheetName != null) {
                    summaryRows = XlsxUtils.readSheet(latestXlsx.getAbsolutePath(), summarySheetName);
                } else {
                    System.out.println("Step 9a: 'Settlement Summary' not found by name; reading sheet index 0: \""
                            + (sheetNames.isEmpty() ? "N/A" : sheetNames.get(0)) + "\"");
                    summaryRows = XlsxUtils.readSheetByIndex(latestXlsx.getAbsolutePath(), 0);
                }

                // ── Sheet 2: Discount Parameter ──────────────────────────────
                // Try by name (case-insensitive), fall back to index 1
                List<Map<String, String>> paramRows;
                String paramSheetName = sheetNames.stream()
                        .filter(s -> s.equalsIgnoreCase("Discount Parameter"))
                        .findFirst().orElse(null);
                if (paramSheetName != null) {
                    paramRows = XlsxUtils.readSheet(latestXlsx.getAbsolutePath(), paramSheetName);
                } else {
                    System.out.println("Step 9a: 'Discount Parameter' not found by name; reading sheet index 1: \""
                            + (sheetNames.size() > 1 ? sheetNames.get(1) : "N/A") + "\"");
                    paramRows = XlsxUtils.readSheetByIndex(latestXlsx.getAbsolutePath(), 1);
                }

                // ── Persist both sheets to JSON ─────────────────────────── (reuses
                // outer-scope targetDir)
                String settlementJsonPath = SettlementDataStore.buildOutputPath(AGREEMENT_NAME, targetDir);
                SettlementDataStore.save(AGREEMENT_NAME, summaryRows, paramRows, settlementJsonPath);
                System.out.println("\uD83D\uDCC4 Settlement data persisted to: " + settlementJsonPath);

            } else {
                System.out.println("Step 9a: No NEW XLSX appeared within 30 s — skipping settlement data extraction.");
            }

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

            // Step 7 (Phase 3): Evaluate pre-download conditions
            System.out.println("Step 7 (Phase 3): Evaluating pre-download conditions...");
            // Bug #5 fix: targetDir already declared at top of this loop iteration — reuse
            // it
            String settlementJsonPath = SettlementDataStore.buildOutputPath(AGREEMENT_NAME, targetDir);
            SettlementDataStore.SettlementData settlementData = SettlementDataStore.load(settlementJsonPath);

            if (settlementData != null && settlementData.getDiscountParameter() != null) {
                List<utils.DownloadConditionEvaluator> conditions = java.util.Arrays.asList(
                        new utils.SendOrPayFinancialCondition());
                for (utils.DownloadConditionEvaluator condition : conditions) {
                    condition.evaluate(settlementData.getDiscountParameter(), forecastPage);
                }
            } else {
                System.out.println("No Settlement Data found, skipping condition evaluations.");
            }

            // Bug #4 fix: snapshot existing CSVs BEFORE triggering the download
            java.util.Set<String> preCsvNames = new java.util.HashSet<>();
            File csvDirRef = new File(DOWNLOAD_DIR);
            {
                File[] preCsv = csvDirRef.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
                if (preCsv != null)
                    for (File f : preCsv)
                        preCsvNames.add(f.getName());
            }
            System.out.println("[Snapshot] " + preCsvNames.size() + " existing CSV file(s) before download.");

            // Step 8 (Phase 3): Download via Action menu
            System.out.println("Step 8 (Phase 3): Downloading the report...");
            forecastPage.clickActionsMenu();
            forecastPage.selectDownloadFromActions();
            forecastPage.selectCsvOptionInPopup();
            forecastPage.clickDownloadInPopup();

            // Bug #4 fix: wait up to 30 s for a NEW CSV (not in the pre-snapshot)
            System.out.println("Step 9 (Phase 3): Waiting for NEW CSV file to appear...");
            File latestFile = null;
            long csvDeadline = System.currentTimeMillis() + 30_000;
            while (System.currentTimeMillis() < csvDeadline && latestFile == null) {
                File[] currentCsvs = csvDirRef.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
                if (currentCsvs != null) {
                    for (File f : currentCsvs) {
                        if (!preCsvNames.contains(f.getName())) {
                            latestFile = f;
                            break;
                        }
                    }
                }
                if (latestFile == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (latestFile != null) {
                System.out.println("Reading data from: " + latestFile.getName());
                try {
                    List<Map<String, String>> allRows = CsvUtils.readCsv(latestFile.getAbsolutePath());

                    // Check for column existence before processing rows
                    if (!allRows.isEmpty()) {
                        Map<String, String> firstRow = allRows.get(0);
                        if (!firstRow.containsKey("Direction")) {
                            throw new Exception("Column 'Direction' not found in CSV file");
                        }
                        if (!firstRow.containsKey("Traffic Period")) {
                            throw new Exception("Column 'Traffic Period' not found in CSV file");
                        }
                        if (!firstRow.containsKey("Discounted Charge EoA")) {
                            throw new Exception("Column 'Discounted Charge EoA' not found in CSV file");
                        }
                        if (!firstRow.containsKey("Discounted Charge Cum")) {
                            throw new Exception("Column 'Discounted Charge Cum' not found in CSV file");
                        }
                    }

                    // ── [1_step_EoA_DATA]: raw input columns ──────────────────
                    List<Map<String, String>> subsetData = allRows.stream().map(row -> {
                        Map<String, String> subset = new java.util.LinkedHashMap<>();
                        subset.put("Direction", row.getOrDefault("Direction", ""));
                        subset.put("Traffic Period", row.getOrDefault("Traffic Period", ""));
                        subset.put("Discount Service Type", row.getOrDefault("Discount Service Type", ""));
                        subset.put("Discount Event Type", row.getOrDefault("Discount Event Type", ""));
                        subset.put("Discount Calculation Type", row.getOrDefault("Discount Calculation Type", ""));
                        subset.put("Discount Basis Value", row.getOrDefault("Discount Basis Value", ""));
                        subset.put("Traffic Volume Restricted EoA",
                                row.getOrDefault("Traffic Volume Restricted EoA", ""));
                        subset.put("TAP Charge EoA", row.getOrDefault("TAP Charge EoA", ""));
                        subset.put("Traffic Volume Restricted Cum",
                                row.getOrDefault("Traffic Volume Restricted Cum", ""));
                        subset.put("TAP Charge Restricted Cum", row.getOrDefault("TAP Charge Restricted Cum", ""));
                        subset.put("Discounted Charge EoA", row.getOrDefault("Discounted Charge EoA", ""));
                        subset.put("Discounted Charge Cum", row.getOrDefault("Discounted Charge Cum", ""));
                        return subset;
                    }).collect(Collectors.toList());

                    System.out.println("[1_step_EoA_DATA] captured " + subsetData.size() + " rows.");

                    // ── Step 10: Manual Calculation ───────────────────────────
                    // Formula:
                    // Discount Charge EoA = Traffic Volume Restricted EoA × Discount Basis Value
                    // Discount Achieved EoA = TAP Charge EoA − Discount Charge EoA
                    // Average Rate EoA = Discount Charge EoA / Traffic Volume Restricted EoA
                    System.out.println("\n--- Step 10: Manual Calculation Results ---");

                    // ── [Send or Pay Financial] Lower Bound extraction ────────────
                    // Read once per agreement from the Discount Parameter XLSX sheet.
                    boolean sendOrPayActive = false;
                    double lowerBound = 0.0;
                    if (settlementData != null && settlementData.getDiscountParameter() != null) {
                        for (Map<String, String> pRow : settlementData.getDiscountParameter()) {
                            if ("Send or Pay Financial (All Services)".equals(pRow.get("Calculation Type"))) {
                                sendOrPayActive = true;
                                lowerBound = parseNum(pRow.getOrDefault("Lower Bound", "0"));
                                break;
                            }
                        }
                    }
                    System.out.println("[CommitmentCheck] sendOrPayActive=" + sendOrPayActive
                            + (sendOrPayActive ? ", lowerBound=" + fmt(lowerBound) : ""));

                    // Build result rows
                    List<Map<String, String>> calcResults = new java.util.ArrayList<>();
                    Map<String, AggregatedData> aggregations = new java.util.LinkedHashMap<>();
                    for (Map<String, String> row : subsetData) {
                        try {
                            double basisValue = parseNum(row.getOrDefault("Discount Basis Value", "0"));
                            double volEoA = parseNum(row.getOrDefault("Traffic Volume Restricted EoA", "0"));
                            double tapEoA = parseNum(row.getOrDefault("TAP Charge EoA", "0"));
                            double volCum = parseNum(row.getOrDefault("Traffic Volume Restricted Cum", "0"));
                            double tapCum = parseNum(row.getOrDefault("TAP Charge Restricted Cum", "0"));

                            // Formulas
                            double chargeEoA;
                            double chargeCum;

                            String calcType = row.getOrDefault("Discount Calculation Type", "");

                            // --- Discount Charge EoA Override (checks Discount Calculation Type) ---
                            if ("Calculated Value of Undiscounted Units".equals(calcType)) {
                                // Case A1: Read Discounted Charge EoA directly from CSV
                                chargeEoA = parseNum(row.get("Discounted Charge EoA"));
                            } else if ("Undiscounted Premium Numbers".equals(calcType)) {
                                // Case A2: Use TAP Charge EoA of same row
                                chargeEoA = tapEoA;
                            } else {
                                // Standard formula (unchanged)
                                chargeEoA = volEoA * basisValue;
                            }

                            // --- Discount Charge Cum Override (checks Discount Calculation Type) ---
                            if ("Calculated Value of Undiscounted Units".equals(calcType)) {
                                // Case B1: Read Discounted Charge Cum directly from CSV
                                chargeCum = parseNum(row.get("Discounted Charge Cum"));
                            } else if ("Undiscounted Premium Numbers".equals(calcType)) {
                                // Case B2: Use TAP Charge Restricted Cum of same row
                                chargeCum = tapCum;
                            } else {
                                // Standard formula (unchanged)
                                chargeCum = volCum * basisValue;
                            }

                            // EXCEPTION: If Calc Type = "Calculated Value of Undiscounted Units",
                            // Discount Achieved EoA must be 0 (do NOT apply the default formula).
                            double achievedEoA = "Calculated Value of Undiscounted Units".equals(calcType)
                                    ? 0.0
                                    : tapEoA - chargeEoA;
                            double rateEoA = (volEoA != 0) ? chargeEoA / volEoA : 0.0;

                            double achievedCum = tapCum - chargeCum;
                            double rateCum = (volCum != 0) ? chargeCum / volCum : 0.0;

                            Map<String, String> result = new java.util.LinkedHashMap<>();
                            result.put("Direction", row.get("Direction"));
                            result.put("Traffic Period", row.get("Traffic Period"));
                            result.put("Service Type", row.get("Discount Service Type"));
                            result.put("Event Type", row.get("Discount Event Type"));
                            result.put("Calc Type", row.get("Discount Calculation Type"));
                            result.put("Basis Value", fmt(basisValue));

                            result.put("Vol EoA", fmt(volEoA));
                            result.put("TAP EoA", fmt(tapEoA));
                            result.put("Discount Charge EoA", fmt(chargeEoA));
                            result.put("Discount Achieved EoA", fmt(achievedEoA));
                            result.put("Avg Rate EoA", fmt(rateEoA));

                            result.put("Vol Cum", fmt(volCum));
                            result.put("TAP Cum", fmt(tapCum));
                            result.put("Discount Charge Cum", fmt(chargeCum));
                            result.put("Discount Achieved Cum", fmt(achievedCum));
                            result.put("Avg Rate Cum", fmt(rateCum));

                            calcResults.add(result);

                            // Build aggregation key and accumulate totals
                            String key = row.get("Direction") + "|" + row.get("Traffic Period");
                            AggregatedData agg = aggregations.computeIfAbsent(key, k -> new AggregatedData());
                            agg.volEoA += volEoA;
                            agg.chargeEoA += chargeEoA;
                            agg.achievedEoA += achievedEoA;
                            agg.volCum += volCum;
                            agg.chargeCum += chargeCum;
                            agg.achievedCum += achievedCum;
                        } catch (NumberFormatException ex) {
                            System.err.println("Skipping row due to parse error: " + ex.getMessage());
                        }
                    }

                    // Print formatted ASCII table
                    printTable(calcResults);

                    // ── [Send or Pay Financial] Commitment condition check ────────
                    // Condition: sendOrPayActive AND SUM(chargeEoA across all rows) < lowerBound
                    boolean commitmentNotAchieved = false;
                    double totalChargeEoA = 0.0;
                    if (sendOrPayActive) {
                        for (Map<String, String> r : calcResults) {
                            totalChargeEoA += parseNum(r.get("Discount Charge EoA"));
                        }
                        commitmentNotAchieved = totalChargeEoA < lowerBound;
                        System.out.println("[CommitmentCheck] totalChargeEoA=" + fmt(totalChargeEoA)
                                + ", lowerBound=" + fmt(lowerBound)
                                + ", commitmentNotAchieved=" + commitmentNotAchieved);
                    }

                    // ── [Send or Pay Financial] Per-row override ─────────────────
                    // Applies ONLY when commitmentNotAchieved == true.
                    if (commitmentNotAchieved) {
                        System.out.println("[CommitmentCheck] Applying commitment override (Discounted Charge EoA < Lower Bound).");
                        for (Map<String, String> r : calcResults) {
                            double chEoA  = parseNum(r.get("Discount Charge EoA"));
                            double tEoA   = parseNum(r.get("TAP EoA"));
                            double vEoA   = parseNum(r.get("Vol EoA"));
                            double vCum   = parseNum(r.get("Vol Cum"));
                            double tCum   = parseNum(r.get("TAP Cum"));

                            // Step 2 – Share Split
                            double split    = (totalChargeEoA != 0) ? chEoA / totalChargeEoA : 0.0;
                            // Step 3 – New Discount Charge EoA
                            double newCh    = split * lowerBound;
                            // Step 4 – Override Discount Achieved EoA
                            double newAch   = tEoA - newCh;
                            // Step 5 – Override Avg Rate EoA
                            double newRate  = (vEoA != 0) ? newCh / vEoA : 0.0;
                            // Step 6 – Override Discount Charge Cum  = Avg Rate EoA * Vol Cum
                            double newChCum  = newRate * vCum;
                            // Step 6b – Cascade: Discount Achieved Cum = TAP Cum - Discount Charge Cum
                            double newAchCum = tCum - newChCum;
                            // Step 6c – Cascade: Avg Rate Cum = Discount Charge Cum / Vol Cum
                            double newRateCum = (vCum != 0) ? newChCum / vCum : 0.0;

                            // Step 1 – Commitment label
                            r.put("Commitment",              "Commitment Not Achieved");
                            r.put("Share Split",             fmt(split));
                            r.put("New Discount Charge EoA", fmt(newCh));
                            // Overrides (Steps 4 & 5)
                            r.put("Discount Achieved EoA",   fmt(newAch));
                            r.put("Avg Rate EoA",            fmt(newRate));
                            // Overrides (Step 6 cascades)
                            r.put("Discount Charge Cum",    fmt(newChCum));
                            r.put("Discount Achieved Cum",  fmt(newAchCum));
                            r.put("Avg Rate Cum",           fmt(newRateCum));
                        }

                        // Rebuild aggregation totals from the overridden per-row values
                        // so the TOTAL row in the HTML report reflects the correct numbers.
                        for (AggregatedData agg : aggregations.values()) {
                            agg.achievedEoA = 0.0;
                            agg.chargeCum   = 0.0;
                            agg.achievedCum = 0.0;
                        }
                        for (Map<String, String> r : calcResults) {
                            String aggKey = r.get("Direction") + "|" + r.get("Traffic Period");
                            AggregatedData agg = aggregations.get(aggKey);
                            if (agg != null) {
                                agg.achievedEoA += parseNum(r.get("Discount Achieved EoA"));
                                agg.chargeCum   += parseNum(r.get("Discount Charge Cum"));
                                agg.achievedCum += parseNum(r.get("Discount Achieved Cum"));
                            }
                        }
                        System.out.println("[CommitmentCheck] Aggregation totals rebuilt from overridden values.");
                    }

                    // Save as HTML report for easy cross-verification.
                    // Report filename is unique per agreement to avoid overwrites.
                    String safeAgreementName = AGREEMENT_NAME.replaceAll("[^a-zA-Z0-9]", "_");
                    String reportPath = System.getProperty("user.dir") + File.separator + "target"
                            + File.separator + "manual_calculation_report_" + safeAgreementName + ".html";
                    writeHtmlReport(calcResults, aggregations, reportPath, AGREEMENT_NAME, commitmentNotAchieved, lowerBound);
                    System.out.println("\n\uD83D\uDCC4 HTML Calculation Report saved to: " + reportPath);

                } catch (Exception e) {
                    System.err.println("Failed to read CSV file: " + e.getMessage());
                }
            } else {
                System.err.println("No NEW CSV appeared within 30 s in: " + DOWNLOAD_DIR);
            }

            System.out.println("\n✅ Phase 3 + Manual Calculation completed for agreement: " + AGREEMENT_NAME);

            // Navigation back to Discount Agreements and the two-step Action-menu
            // reset are now performed at the START of the NEXT iteration (above),
            // so no end-of-loop reset is needed here.

        } // end for (AGREEMENT_NAME : enabledAgreements)

        // ---------------------------------------------------------------
        // Post-run cleanup: remove stale files for disabled agreements
        // Targets:  manual_calculation_report_*.html
        //           settlement_data_*.json
        // ---------------------------------------------------------------
        System.out.println("\n[Cleanup] Removing stale report/data files for disabled agreements...");

        // Build expected filenames from the currently-enabled agreements
        java.util.Set<String> expectedHtmlNames = new java.util.HashSet<>();
        java.util.Set<String> expectedJsonNames = new java.util.HashSet<>();
        for (String name : enabledAgreements) {
            String safe = name.replaceAll("[^a-zA-Z0-9]", "_");
            expectedHtmlNames.add("manual_calculation_report_" + safe + ".html");
            expectedJsonNames.add("settlement_data_" + safe + ".json");
        }

        File targetDirFile = new File(System.getProperty("user.dir") + File.separator + "target");

        // --- Clean up stale HTML reports ---
        File[] allHtmlFiles = targetDirFile.listFiles(
                (d, n) -> n.startsWith("manual_calculation_report_") && n.endsWith(".html"));
        int htmlDeleted = 0;
        if (allHtmlFiles != null) {
            for (File htmlFile : allHtmlFiles) {
                if (!expectedHtmlNames.contains(htmlFile.getName())) {
                    boolean deleted = htmlFile.delete();
                    System.out.println("[Cleanup] HTML " + (deleted ? "Deleted" : "FAILED to delete") + ": " + htmlFile.getName());
                    if (deleted) htmlDeleted++;
                }
            }
        }

        // --- Clean up stale settlement JSON files ---
        File[] allJsonFiles = targetDirFile.listFiles(
                (d, n) -> n.startsWith("settlement_data_") && n.endsWith(".json"));
        int jsonDeleted = 0;
        if (allJsonFiles != null) {
            for (File jsonFile : allJsonFiles) {
                if (!expectedJsonNames.contains(jsonFile.getName())) {
                    boolean deleted = jsonFile.delete();
                    System.out.println("[Cleanup] JSON " + (deleted ? "Deleted" : "FAILED to delete") + ": " + jsonFile.getName());
                    if (deleted) jsonDeleted++;
                }
            }
        }

        System.out.println("[Cleanup] Done — deleted " + htmlDeleted + " HTML report(s) and "
                + jsonDeleted + " settlement JSON(s) for disabled agreements.");

        System.out.println("\n✅ All enabled agreements processed successfully!");
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /** Parse a number string that may contain commas (e.g. "134,338.17"). */
    private static double parseNum(String raw) {
        if (raw == null || raw.trim().isEmpty())
            return 0.0;
        return Double.parseDouble(raw.trim().replace(",", ""));
    }

    /** Format a double to 5 decimal places. */
    private static String fmt(double val) {
        return String.format("%.5f", val);
    }

    /**
     * Print a list of rows (LinkedHashMap preserves column order) as a
     * left-aligned ASCII table with dynamic column widths.
     */
    private static void printTable(List<Map<String, String>> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("(no data to display)");
            return;
        }

        // Determine column names and max widths
        List<String> cols = new java.util.ArrayList<>(rows.get(0).keySet());
        Map<String, Integer> widths = new java.util.LinkedHashMap<>();
        for (String col : cols) {
            widths.put(col, col.length());
        }
        for (Map<String, String> row : rows) {
            for (String col : cols) {
                String val = row.getOrDefault(col, "");
                widths.put(col, Math.max(widths.get(col), val.length()));
            }
        }

        // Build separator and header
        StringBuilder sep = new StringBuilder("+");
        StringBuilder header = new StringBuilder("|");
        for (String col : cols) {
            int w = widths.get(col);
            sep.append("-".repeat(w + 2)).append("+");
            header.append(String.format(" %-" + w + "s |", col));
        }

        System.out.println(sep);
        System.out.println(header);
        System.out.println(sep);

        // Print rows
        for (Map<String, String> row : rows) {
            StringBuilder line = new StringBuilder("|");
            for (String col : cols) {
                int w = widths.get(col);
                line.append(String.format(" %-" + w + "s |", row.getOrDefault(col, "")));
            }
            System.out.println(line);
        }
        System.out.println(sep);
    }

    /**
     * Write calculation results as a styled HTML report for easy
     * cross-verification.
     * Columns are color-coded: blue = input, green = calculated result.
     */
    private static void writeHtmlReport(List<Map<String, String>> rows, Map<String, AggregatedData> aggregations,
            String filePath, String agreementName, boolean commitmentNotAchieved, double lowerBound) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<title>Manual Discount Calculation Report</title>\n")
                .append("<style>\n")
                .append("  body { font-family: Arial, sans-serif; font-size: 12px; margin: 20px; background:#f0f2f5; }\n")
                .append("  h1 { color: #2c3e50; font-size: 18px; margin-bottom: 2px; }\n")
                .append("  p.sub { color: #666; margin-top: 0; font-size: 11px; }\n")
                .append("  .legend { margin-bottom: 10px; }\n")
                .append("  .legend span { display:inline-block; padding:3px 10px; border-radius:4px; margin-right:8px; font-size:11px; }\n")
                .append("  .inp   { background:#d6eaf8; color:#1a5276; }\n")
                .append("  .eoa   { background:#d5f5e3; color:#1e8449; font-weight:bold; }\n")
                .append("  .cum   { background:#fdebd0; color:#784212; font-weight:bold; }\n")
                .append("  table { border-collapse: collapse; width:100%; background:#fff; box-shadow:0 1px 4px rgba(0,0,0,.1); font-size:11px; }\n")
                .append("  th { padding:7px 10px; text-align:center; white-space:nowrap; }\n")
                .append("  th.inp  { background:#2e86c1; color:#fff; }\n")
                .append("  th.eoa  { background:#1e8449; color:#fff; }\n")
                .append("  th.cum  { background:#d35400; color:#fff; }\n")
                .append("  td { padding:6px 10px; text-align:right; border-bottom:1px solid #e8e8e8; white-space:nowrap; }\n")
                .append("  td.txt { text-align:left; }\n")
                .append("  tr:nth-child(even) td { background:#f9f9f9; }\n")
                .append("  tr:hover td { background:#fef9e7 !important; }\n")
                .append("  .formula { background:#fdfefe; border:1px solid #d5d8dc; border-radius:6px; padding:10px 16px; margin-top:16px; font-size:11px; color:#444; line-height:1.8; }\n")
                .append("  .formula b { color:#1a5276; }\n")
                .append("</style>\n</head>\n<body>\n")
                .append("<h1>\uD83D\uDCCA Manual Discount Calculation Report</h1>\n")
                .append("<p class=\"sub\">Generated by IotronAutomationTest &mdash; Agreement: <b>")
                .append(agreementName).append("</b></p>\n")
                .append("<div class=\"legend\">\n")
                .append("  <span class=\"inp\">&#9632; Input data from CSV</span>\n")
                .append("  <span class=\"eoa\">&#9632; EoA Calculated</span>\n")
                .append("  <span class=\"cum\">&#9632; Cum Calculated</span>\n")
                .append("</div>\n");

        // Table header
        html.append("<table>\n<thead><tr>\n")
                .append("  <th class=\"inp\">Direction</th>\n")
                .append("  <th class=\"inp\">Traffic Period</th>\n")
                .append("  <th class=\"inp\">Service Type</th>\n")
                .append("  <th class=\"inp\">Event Type</th>\n")
                .append("  <th class=\"inp\">Calc Type</th>\n")
                .append("  <th class=\"inp\">Basis Value</th>\n")
                .append("  <th class=\"inp\">Traffic Volume Restricted EoA</th>\n")
                .append("  <th class=\"inp\">TAP Charge EoA</th>\n")
                .append("  <th class=\"inp\">Traffic Volume Restricted Cum</th>\n")
                .append("  <th class=\"inp\">TAP Charge Restricted Cum</th>\n")
                .append("  <th class=\"eoa\">Discount Charge EoA<br><small>= Vol EoA &times; Basis</small></th>\n");
        if (commitmentNotAchieved) {
            html.append("  <th class=\"eoa\">Commitment</th>\n")
                .append("  <th class=\"eoa\">Share Split<br><small>= Charge EoA / &sum; Charge EoA</small></th>\n")
                .append("  <th class=\"eoa\">New Discount Charge EoA<br><small>= Share Split &times; Lower Bound (")
                .append(fmt(lowerBound)).append(")</small></th>\n");
        }
        html.append("  <th class=\"eoa\">Discount Achieved EoA<br><small>= TAP EoA &minus; Charge</small></th>\n")
                .append("  <th class=\"eoa\">Avg Rate EoA<br><small>= Charge / Vol</small></th>\n")
                .append("  <th class=\"cum\">Discount Charge Cum<br><small>= Vol Cum &times; Basis</small></th>\n")
                .append("  <th class=\"cum\">Discount Achieved Cum<br><small>= TAP Cum &minus; Charge</small></th>\n")
                .append("  <th class=\"cum\">Avg Rate Cum<br><small>= Charge / Vol</small></th>\n")
                .append("</tr></thead>\n<tbody>\n");

        // Group rows by Direction|Traffic Period
        Map<String, List<Map<String, String>>> groupedRows = new java.util.LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String key = row.get("Direction") + "|" + row.get("Traffic Period");
            groupedRows.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<Map<String, String>>> groupEntry : groupedRows.entrySet()) {
            String key = groupEntry.getKey();
            for (Map<String, String> row : groupEntry.getValue()) {
                html.append("<tr>");
                html.append("<td class=\"txt\">").append(esc(row.get("Direction"))).append("</td>");
                html.append("<td class=\"txt\">").append(esc(row.get("Traffic Period"))).append("</td>");
                html.append("<td class=\"txt\">").append(esc(row.get("Service Type"))).append("</td>");
                html.append("<td class=\"txt\">").append(esc(row.get("Event Type"))).append("</td>");
                html.append("<td class=\"txt\">").append(esc(row.get("Calc Type"))).append("</td>");
                html.append("<td>").append(esc(row.get("Basis Value"))).append("</td>");
                html.append("<td>").append(esc(row.get("Vol EoA"))).append("</td>");
                html.append("<td>").append(esc(row.get("TAP EoA"))).append("</td>");
                html.append("<td>").append(esc(row.get("Vol Cum"))).append("</td>");
                html.append("<td>").append(esc(row.get("TAP Cum"))).append("</td>");

                html.append("<td style=\"background:#eafaf1;\">").append(esc(row.get("Discount Charge EoA")))
                        .append("</td>");
                if (commitmentNotAchieved) {
                    html.append("<td class=\"txt\" style=\"background:#fde8d8; color:#7b241c; font-weight:bold;\">").append(esc(row.getOrDefault("Commitment", ""))).append("</td>");
                    html.append("<td style=\"background:#fde8d8; color:#7b241c;\">").append(esc(row.getOrDefault("Share Split", ""))).append("</td>");
                    html.append("<td style=\"background:#fde8d8; color:#7b241c; font-weight:bold;\">").append(esc(row.getOrDefault("New Discount Charge EoA", ""))).append("</td>");
                }
                html.append("<td style=\"background:#eafaf1;\">").append(esc(row.get("Discount Achieved EoA")))
                        .append("</td>");
                html.append("<td style=\"background:#eafaf1;\">").append(esc(row.get("Avg Rate EoA"))).append("</td>");

                html.append("<td style=\"background:#fef5e7;\">").append(esc(row.get("Discount Charge Cum")))
                        .append("</td>");
                html.append("<td style=\"background:#fef5e7;\">").append(esc(row.get("Discount Achieved Cum")))
                        .append("</td>");
                html.append("<td style=\"background:#fef5e7;\">").append(esc(row.get("Avg Rate Cum"))).append("</td>");
                html.append("</tr>\n");
            }

            // Add aggregated total row for the group
            AggregatedData agg = aggregations.get(key);
            if (agg != null) {
                html.append("<tr style=\"font-weight:bold; background-color:#eaecee;\">");
                html.append("<td colspan=\"6\" class=\"txt\" style=\"text-align:right;\">TOTAL ("
                        + esc(key.replace("|", " - ")) + ")</td>");
                html.append("<td>").append(fmt(agg.volEoA)).append("</td>");
                html.append("<td></td>");
                html.append("<td>").append(fmt(agg.volCum)).append("</td>");
                html.append("<td></td>");
                html.append("<td style=\"background:#d5f5e3; color:#1e8449;\">").append(fmt(agg.chargeEoA))
                        .append("</td>");
                if (commitmentNotAchieved) {
                    html.append("<td></td>");
                    html.append("<td></td>");
                    html.append("<td></td>");
                }
                html.append("<td style=\"background:#d5f5e3; color:#1e8449;\">").append(fmt(agg.achievedEoA))
                        .append("</td>");
                html.append("<td style=\"background:#d5f5e3; color:#1e8449;\"></td>");
                html.append("<td style=\"background:#fdebd0; color:#784212;\">").append(fmt(agg.chargeCum))
                        .append("</td>");
                html.append("<td style=\"background:#fdebd0; color:#784212;\">").append(fmt(agg.achievedCum))
                        .append("</td>");
                html.append("<td style=\"background:#fdebd0; color:#784212;\"></td>");
                html.append("</tr>\n");
            }
        }

        html.append("</tbody>\n</table>\n");

        // Formula legend
        html.append("<div class=\"formula\">\n")
                .append("<b>Calculation Formulas:</b><br>\n")
                .append("&nbsp;&nbsp;Discount Charge EoA = <b>Traffic Volume Restricted EoA</b> &times; <b>Discount Basis Value</b><br>\n")
                .append("&nbsp;&nbsp;Discount Achieved EoA = <b>TAP Charge EoA</b> &minus; <b>Discount Charge EoA</b><br>\n")
                .append("&nbsp;&nbsp;Avg Rate EoA = <b>Discount Charge EoA</b> &divide; <b>Traffic Volume Restricted EoA</b><br>\n")
                .append("&nbsp;&nbsp;Discount Charge Cum = <b>Traffic Volume Restricted Cum</b> &times; <b>Discount Basis Value</b><br>\n")
                .append("&nbsp;&nbsp;Discount Achieved Cum = <b>TAP Charge Restricted Cum</b> &minus; <b>Discount Charge Cum</b><br>\n")
                .append("&nbsp;&nbsp;Avg Rate Cum = <b>Discount Charge Cum</b> &divide; <b>Traffic Volume Restricted Cum</b>\n");
        if (commitmentNotAchieved) {
            html.append("<br><b>Send or Pay Financial &mdash; Commitment Not Achieved Overrides:</b><br>\n")
                    .append("&nbsp;&nbsp;Share Split = <b>Discount Charge EoA (row)</b> &divide; <b>&sum; Discount Charge EoA (all rows)</b><br>\n")
                    .append("&nbsp;&nbsp;New Discount Charge EoA = <b>Share Split</b> &times; <b>Lower Bound (" + fmt(lowerBound) + ")</b><br>\n")
                    .append("&nbsp;&nbsp;Discount Achieved EoA <i>(overridden)</i> = <b>TAP Charge EoA</b> &minus; <b>New Discount Charge EoA</b><br>\n")
                    .append("&nbsp;&nbsp;Avg Rate EoA <i>(overridden)</i> = <b>New Discount Charge EoA</b> &divide; <b>Traffic Volume Restricted EoA</b>\n");
        }
        html.append("</div>\n");

        html.append("</body>\n</html>\n");

        try (java.io.FileWriter fw = new java.io.FileWriter(filePath)) {
            fw.write(html.toString());
        } catch (java.io.IOException e) {
            System.err.println("[writeHtmlReport] Failed to write HTML report: " + e.getMessage());
        }
    }

    /** HTML-escape a string. */
    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Aggregated Data model */
    public static class AggregatedData {
        public double volEoA = 0.0;
        public double chargeEoA = 0.0;
        public double achievedEoA = 0.0;
        public double volCum = 0.0;
        public double chargeCum = 0.0;
        public double achievedCum = 0.0;
    }
}
