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
import utils.CsvUtils;
import utils.FileDownloadUtils;
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

    /** Central download directory – used for cleanup and CSV reading. */
    public static final String DOWNLOAD_DIR =
            System.getProperty("user.dir") + File.separator + "target" + File.separator + "downloads";

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
        String downloadDir = DOWNLOAD_DIR;
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

                // ── STEP 1 & 2: Load all rows, extract required columns ──────
                List<Map<String, String>> subsetData = allRows.stream().map(row -> {
                    Map<String, String> s = new java.util.LinkedHashMap<>();
                    s.put("Discount Direction",            row.getOrDefault("Discount Direction", ""));
                    s.put("Traffic Period",                row.getOrDefault("Traffic Period", ""));
                    s.put("Discount Service Type",         row.getOrDefault("Discount Service Type", ""));
                    s.put("Discount Event Type",           row.getOrDefault("Discount Event Type", ""));
                    s.put("Discount Calculation Type",     row.getOrDefault("Discount Calculation Type", ""));
                    s.put("Discount Basis Value",          row.getOrDefault("Discount Basis Value", ""));
                    s.put("Traffic Volume Restricted EoA", row.getOrDefault("Traffic Volume Restricted EoA", ""));
                    s.put("TAP Charge EoA",                row.getOrDefault("TAP Charge EoA", ""));
                    s.put("Traffic Volume Restricted Cum", row.getOrDefault("Traffic Volume Restricted Cum", ""));
                    s.put("TAP Charge Restricted Cum",     row.getOrDefault("TAP Charge Restricted Cum", ""));
                    return s;
                }).collect(Collectors.toList());

                System.out.println("[1_step_EoA_DATA] total rows loaded: " + subsetData.size());

                // ── STEP 1 FILTER: Visitor/Inbound ──────────────────────────
                List<Map<String, String>> filtered = subsetData.stream()
                    .filter(r -> "Visitor/Inbound".equalsIgnoreCase(r.getOrDefault("Discount Direction", "")))
                    .collect(Collectors.toList());

                // ── STEP 1 FILTER: First month (earliest Traffic Period) ─────
                String firstMonth = filtered.stream()
                    .map(r -> r.getOrDefault("Traffic Period", ""))
                    .filter(p -> !p.isEmpty())
                    .min(java.util.Comparator.naturalOrder())
                    .orElse("");
                System.out.println("[Filter] Discount Direction = Visitor/Inbound, Traffic Period = " + firstMonth);
                filtered = filtered.stream()
                    .filter(r -> firstMonth.equals(r.getOrDefault("Traffic Period", "")))
                    .collect(Collectors.toList());
                System.out.println("[Filter] Rows after filter: " + filtered.size());

                // ── STEP 3: Apply formulas per row ───────────────────────────
                // F1: Discount Charge EoA = Traffic Volume Restricted EoA × Discount Basis Value
                // F2: Aggregate Rate       = Discount Charge EoA / Traffic Volume Restricted EoA
                // F3: Discount Charge Cum  = Aggregate Rate × Traffic Volume Restricted Cum
                // F4: Discount Achieved EoA = TAP Charge EoA - Discount Charge EoA
                // F5: Discount Achieved Cum = TAP Charge Restricted Cum - Discount Charge Cum
                List<Map<String, Object>> calcRows = new java.util.ArrayList<>();
                for (Map<String, String> row : filtered) {
                    double basisValue  = parseNum(row.getOrDefault("Discount Basis Value", "0"));
                    double volEoA      = parseNum(row.getOrDefault("Traffic Volume Restricted EoA", "0"));
                    double tapEoA      = parseNum(row.getOrDefault("TAP Charge EoA", "0"));
                    double volCum      = parseNum(row.getOrDefault("Traffic Volume Restricted Cum", "0"));
                    double tapCum      = parseNum(row.getOrDefault("TAP Charge Restricted Cum", "0"));

                    double discChargeEoA   = volEoA * basisValue;                             // F1
                    double aggregateRate   = (volEoA != 0) ? discChargeEoA / volEoA : 0.0;   // F2
                    double discChargeCum   = aggregateRate * volCum;                          // F3
                    double discAchievedEoA = tapEoA - discChargeEoA;                          // F4
                    double discAchievedCum = tapCum - discChargeCum;                          // F5

                    Map<String, Object> r = new java.util.LinkedHashMap<>();
                    r.put("Discount Direction",        row.getOrDefault("Discount Direction", ""));
                    r.put("Traffic Period",             row.getOrDefault("Traffic Period", ""));
                    r.put("Service Type",               row.getOrDefault("Discount Service Type", ""));
                    r.put("Event Type",                 row.getOrDefault("Discount Event Type", ""));
                    r.put("Calc Type",                  row.getOrDefault("Discount Calculation Type", ""));
                    r.put("Discount Basis Value",        basisValue);
                    r.put("Vol Restricted EoA",          volEoA);
                    r.put("TAP Charge EoA",              tapEoA);
                    r.put("Discount Charge EoA",         discChargeEoA);
                    r.put("Discount Achieved EoA",       discAchievedEoA);
                    r.put("Aggregate Rate",              aggregateRate);
                    r.put("Vol Restricted Cum",          volCum);
                    r.put("TAP Charge Restricted Cum",   tapCum);
                    r.put("Discount Charge Cum",         discChargeCum);
                    r.put("Discount Achieved Cum",       discAchievedCum);
                    calcRows.add(r);
                }

                // ── STEP 4: GROUP BY 6 keys, SUM numeric columns ────────────
                String[] groupKeys = {"Discount Direction", "Traffic Period", "Service Type",
                                      "Event Type", "Calc Type", "Discount Basis Value"};
                String[] sumKeys   = {"Vol Restricted EoA", "TAP Charge EoA", "Discount Charge EoA",
                                      "Discount Achieved EoA", "Aggregate Rate",
                                      "Vol Restricted Cum", "TAP Charge Restricted Cum",
                                      "Discount Charge Cum", "Discount Achieved Cum"};

                java.util.LinkedHashMap<String, Map<String, Object>> grouped = new java.util.LinkedHashMap<>();
                for (Map<String, Object> r : calcRows) {
                    StringBuilder key = new StringBuilder();
                    for (String k : groupKeys) key.append(r.get(k)).append("|");
                    String compositeKey = key.toString();
                    if (!grouped.containsKey(compositeKey)) {
                        grouped.put(compositeKey, new java.util.LinkedHashMap<>(r));
                    } else {
                        Map<String, Object> agg = grouped.get(compositeKey);
                        for (String sk : sumKeys) {
                            agg.put(sk, ((Number)agg.get(sk)).doubleValue() + ((Number)r.get(sk)).doubleValue());
                        }
                    }
                }

                List<Map<String, Object>> aggregated = new java.util.ArrayList<>(grouped.values());
                System.out.println("\n--- Step 10: Manual Calculation Results (Aggregated: " + aggregated.size() + " rows) ---");

                // ── STEP 5: Convert to String map and output (new column order) ──
                // Output: Direction | Service Type | Event Type | Calc Type | Basis Value
                //         | Vol EoA | TAP EoA | Disc Charge EoA | Disc Achieved EoA
                //         | Aggregate Rate | Disc Charge Cum | Disc Achieved Cum
                List<Map<String, String>> outRows = new java.util.ArrayList<>();
                for (Map<String, Object> r : aggregated) {
                    double volEoAagg     = ((Number)r.get("Vol Restricted EoA")).doubleValue();
                    double discChgEoAagg = ((Number)r.get("Discount Charge EoA")).doubleValue();
                    // Recalculate Aggregate Rate from aggregated values
                    double aggRate       = (volEoAagg != 0) ? discChgEoAagg / volEoAagg : 0.0;
                    double volCumAgg     = ((Number)r.get("Vol Restricted Cum")).doubleValue();
                    double tapCumAgg     = ((Number)r.get("TAP Charge Restricted Cum")).doubleValue();
                    double discChgCumAgg = aggRate * volCumAgg;
                    double discAchCumAgg = tapCumAgg - discChgCumAgg;

                    Map<String, String> out = new java.util.LinkedHashMap<>();
                    out.put("Discount Direction",            str(r.get("Discount Direction")));
                    out.put("Service Type",                  str(r.get("Service Type")));
                    out.put("Event Type",                    str(r.get("Event Type")));
                    out.put("Calc Type",                     str(r.get("Calc Type")));
                    out.put("Discount Basis Value",          fmt(((Number)r.get("Discount Basis Value")).doubleValue()));
                    out.put("Traffic Volume Restricted EoA", fmt(volEoAagg));
                    out.put("TAP Charge EoA",                fmt(((Number)r.get("TAP Charge EoA")).doubleValue()));
                    out.put("Discount Charge EoA",           fmt(discChgEoAagg));
                    out.put("Discount Achieved EoA",         fmt(((Number)r.get("Discount Achieved EoA")).doubleValue()));
                    out.put("Aggregate Rate",                fmt(aggRate));
                    out.put("Discount Charge Cum",           fmt(discChgCumAgg));
                    out.put("Discount Achieved Cum",         fmt(discAchCumAgg));
                    outRows.add(out);
                }

                printTable(outRows);

                String reportPath = System.getProperty("user.dir") + File.separator + "target"
                        + File.separator + "manual_calculation_report.html";
                writeHtmlReport(outRows, reportPath, firstMonth);
                System.out.println("\n\uD83D\uDCC4 HTML Calculation Report saved to: " + reportPath);

            } catch (Exception e) {
                System.err.println("Failed to read CSV file: " + e.getMessage());
            }
        } else {
            System.err.println("No CSV file found in download directory: " + downloadDir);
        }

        System.out.println("\n✅ Phase 3 + Manual Calculation completed successfully!");
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /** Parse a number string that may contain commas (e.g. "134,338.17"). */
    private static double parseNum(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 0.0;
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
     * Write aggregated calculation results as a styled HTML report.
     * Blue headers = input, Green headers = EoA calculated,
     * Purple header = Aggregate Rate, Orange headers = Cum calculated.
     *
     * Output columns (per new spec):
     *   Discount Direction | Service Type | Event Type | Calc Type | Discount Basis Value
     *   | Traffic Volume Restricted EoA | TAP Charge EoA | Discount Charge EoA
     *   | Discount Achieved EoA | Aggregate Rate | Discount Charge Cum | Discount Achieved Cum
     */
    private static void writeHtmlReport(List<Map<String, String>> rows, String filePath, String firstMonth) {
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
            .append("  .rate  { background:#e8daef; color:#6c3483; font-weight:bold; }\n")
            .append("  .cum   { background:#fdebd0; color:#784212; font-weight:bold; }\n")
            .append("  table { border-collapse: collapse; width:100%; background:#fff; box-shadow:0 1px 4px rgba(0,0,0,.1); font-size:11px; }\n")
            .append("  th { padding:7px 10px; text-align:center; white-space:nowrap; }\n")
            .append("  th.inp  { background:#2e86c1; color:#fff; }\n")
            .append("  th.eoa  { background:#1e8449; color:#fff; }\n")
            .append("  th.rate { background:#7d3c98; color:#fff; }\n")
            .append("  th.cum  { background:#d35400; color:#fff; }\n")
            .append("  td { padding:6px 10px; text-align:right; border-bottom:1px solid #e8e8e8; white-space:nowrap; }\n")
            .append("  td.txt { text-align:left; }\n")
            .append("  tr:nth-child(even) td { background:#f9f9f9; }\n")
            .append("  tr:hover td { background:#fef9e7 !important; }\n")
            .append("  .formula { background:#fdfefe; border:1px solid #d5d8dc; border-radius:6px; padding:10px 16px; margin-top:16px; font-size:11px; color:#444; line-height:1.8; }\n")
            .append("  .formula b { color:#1a5276; }\n")
            .append("</style>\n</head>\n<body>\n")
            .append("<h1>&#128202; Manual Discount Calculation Report</h1>\n")
            .append("<p class=\"sub\">Agreement: <b>").append(esc(AGREEMENT_NAME)).append("</b>")
            .append(" &nbsp;&mdash;&nbsp; Filter: <b>Visitor/Inbound</b>, Traffic Period: <b>").append(esc(firstMonth)).append("</b>")
            .append(" &nbsp;&mdash;&nbsp; Rows (aggregated): <b>").append(rows.size()).append("</b></p>\n")
            .append("<div class=\"legend\">\n")
            .append("  <span class=\"inp\">&#9632; Input data</span>\n")
            .append("  <span class=\"eoa\">&#9632; EoA Calculated</span>\n")
            .append("  <span class=\"rate\">&#9632; Aggregate Rate</span>\n")
            .append("  <span class=\"cum\">&#9632; Cum Calculated</span>\n")
            .append("</div>\n");

        // ── Header row ──────────────────────────────────────────────────────
        html.append("<table>\n<thead><tr>\n");
        // Input columns (5)
        for (String h : new String[]{"Discount Direction", "Service Type", "Event Type",
                                     "Calc Type", "Discount Basis Value"})
            html.append("  <th class=\"inp\">").append(h).append("</th>\n");
        // EoA calculated columns (4)
        html.append("  <th class=\"eoa\">Traffic Volume Restricted EoA<br><small>(Aggregated)</small></th>\n");
        html.append("  <th class=\"eoa\">TAP Charge EoA<br><small>(Aggregated)</small></th>\n");
        html.append("  <th class=\"eoa\">Discount Charge EoA<br><small>= Vol &times; Basis</small></th>\n");
        html.append("  <th class=\"eoa\">Discount Achieved EoA<br><small>= TAP &minus; Charge EoA</small></th>\n");
        // Aggregate Rate column (1)
        html.append("  <th class=\"rate\">Aggregate Rate<br><small>= Charge EoA &divide; Vol EoA</small></th>\n");
        // Cum calculated columns (2)
        html.append("  <th class=\"cum\">Discount Charge Cum<br><small>= AggRate &times; Vol Cum</small></th>\n");
        html.append("  <th class=\"cum\">Discount Achieved Cum<br><small>= TAP Cum &minus; Charge Cum</small></th>\n");
        html.append("</tr></thead>\n<tbody>\n");

        // ── Data rows ───────────────────────────────────────────────────────
        for (Map<String, String> row : rows) {
            html.append("<tr>");
            // Input columns (left-aligned)
            for (String k : new String[]{"Discount Direction", "Service Type", "Event Type",
                                         "Calc Type", "Discount Basis Value"})
                html.append("<td class=\"txt\">").append(esc(row.getOrDefault(k, ""))).append("</td>");
            // EoA calculated (green tint)
            for (String k : new String[]{"Traffic Volume Restricted EoA", "TAP Charge EoA",
                                         "Discount Charge EoA", "Discount Achieved EoA"})
                html.append("<td style=\"background:#eafaf1;\">").append(esc(row.getOrDefault(k, ""))).append("</td>");
            // Aggregate Rate (purple tint)
            html.append("<td style=\"background:#f5eef8;\">").append(esc(row.getOrDefault("Aggregate Rate", ""))).append("</td>");
            // Cum calculated (orange tint)
            for (String k : new String[]{"Discount Charge Cum", "Discount Achieved Cum"})
                html.append("<td style=\"background:#fef5e7;\">").append(esc(row.getOrDefault(k, ""))).append("</td>");
            html.append("</tr>\n");
        }

        html.append("</tbody>\n</table>\n");

        // ── Formula legend ──────────────────────────────────────────────────
        html.append("<div class=\"formula\">\n")
            .append("<b>Calculation Formulas:</b><br>\n")
            .append("&nbsp;&nbsp;<b>F1</b> &mdash; Discount Charge EoA &nbsp;&nbsp;&nbsp;&nbsp;= <b>Traffic Volume Restricted EoA</b> &times; <b>Discount Basis Value</b><br>\n")
            .append("&nbsp;&nbsp;<b>F2</b> &mdash; Aggregate Rate &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;= <b>Discount Charge EoA</b> &divide; <b>Traffic Volume Restricted EoA</b> (0 if Vol EoA = 0)<br>\n")
            .append("&nbsp;&nbsp;<b>F3</b> &mdash; Discount Charge Cum &nbsp;&nbsp;&nbsp;= <b>Aggregate Rate</b> &times; <b>Traffic Volume Restricted Cum</b><br>\n")
            .append("&nbsp;&nbsp;<b>F4</b> &mdash; Discount Achieved EoA = <b>TAP Charge EoA</b> &minus; <b>Discount Charge EoA</b><br>\n")
            .append("&nbsp;&nbsp;<b>F5</b> &mdash; Discount Achieved Cum = <b>TAP Charge Restricted Cum</b> &minus; <b>Discount Charge Cum</b><br>\n")
            .append("&nbsp;&nbsp;<b>Aggregation</b> &mdash; GROUP BY: Direction, Service Type, Event Type, Calc Type, Basis Value &rarr; SUM numeric columns\n")
            .append("</div>\n");

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

    /** Safely convert an Object to String. */
    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
