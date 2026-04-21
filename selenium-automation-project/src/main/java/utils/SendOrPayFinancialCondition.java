package utils;

import pages.ForecastReportPage;
import java.util.List;
import java.util.Map;

/**
 * Pre-download Condition 1: "Send or Pay Financial (All Services)"
 *
 * <p><b>Logic:</b>
 * <ul>
 *   <li>Check the Discount Parameter tab rows specifically for the column {@code "Calculation Type"}
 *       containing the value {@code "Send or Pay Financial (All Services)"}.</li>
 *   <li>If found (condition is TRUE): select
 *       {@code "Before \"Send or Pay Financial Commitment\" for Standard EoA"} in the Calculation Type
 *       dropdown on the IOT Calculation Detail Report page (Page 688) BEFORE downloading.</li>
 *   <li>If not found: log and skip — no UI action is taken.</li>
 * </ul>
 */
public class SendOrPayFinancialCondition implements DownloadConditionEvaluator {

    /** Value to look for in the "Calculation Type" column of the Discount Parameter sheet. */
    private static final String TRIGGER_VALUE = "Send or Pay Financial (All Services)";

    /** Exact value to select in the "Calculation Type" dropdown on Page 688. */
    private static final String CALC_TYPE_VALUE = "Before \"Send or Pay Financial Commitment\" for Standard EoA";

    @Override
    public void evaluate(List<Map<String, String>> paramRows, ForecastReportPage forecastPage) {
        System.out.println("[SendOrPayFinancialCondition] Checking Discount Parameter \"Calculation Type\" column for: \""
                + TRIGGER_VALUE + "\"");

        boolean conditionMet = paramRows.stream()
                .anyMatch(row -> TRIGGER_VALUE.equals(row.get("Calculation Type")));

        if (conditionMet) {
            System.out.println("[SendOrPayFinancialCondition] \u2705 Condition MET — \""
                    + TRIGGER_VALUE + "\" found in Calculation Type column.");
            System.out.println("[SendOrPayFinancialCondition] Selecting Calculation Type: \""
                    + CALC_TYPE_VALUE + "\" on Page 688...");
            forecastPage.selectCalculationTypeIfAvailable(CALC_TYPE_VALUE);
            System.out.println("[SendOrPayFinancialCondition] Refreshing page to apply selection...");
            forecastPage.clickDetailRefresh();
            System.out.println("[SendOrPayFinancialCondition] Refresh completed. Selection active.");
        } else {
            System.out.println("[SendOrPayFinancialCondition] \u2139\uFE0F Condition NOT met — \""
                    + TRIGGER_VALUE + "\" not found. Skipping UI selection.");
        }
    }
}
