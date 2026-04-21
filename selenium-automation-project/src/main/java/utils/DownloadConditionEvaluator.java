package utils;

import pages.ForecastReportPage;
import java.util.List;
import java.util.Map;

/**
 * Extensible interface for pre-download conditions on the IOT Calculation Detail Report page.
 *
 * <p>Each implementation checks a specific condition against the Discount Parameter sheet rows
 * and performs a UI action on {@link ForecastReportPage} if the condition is met.
 *
 * <p>To add a new condition:
 * <ol>
 *   <li>Create a new class that implements this interface.</li>
 *   <li>Register it in the condition list inside {@code IotronAutomationTest} (Step 7, Phase 3).</li>
 * </ol>
 */
public interface DownloadConditionEvaluator {

    /**
     * Evaluates the condition against the Discount Parameter sheet rows.
     * If the condition is met, performs the required UI action on the page.
     *
     * @param paramRows   Rows from the "Discount Parameter" sheet of the Settlement XLSX.
     * @param forecastPage The {@link ForecastReportPage} instance for UI interactions on Page 688.
     */
    void evaluate(List<Map<String, String>> paramRows, ForecastReportPage forecastPage);
}
