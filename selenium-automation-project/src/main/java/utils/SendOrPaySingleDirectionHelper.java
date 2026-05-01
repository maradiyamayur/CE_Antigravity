package utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper that determines whether the "Send or Pay Financial (All Services)" condition
 * applies to only ONE Discount Direction (Customer/Outbound OR Visitor/Inbound, but not both).
 *
 * <p>This is used to decide whether a second CSV download with "Standard EoA" is required
 * for the missing direction, per the 1st-Step EoA single-direction logic on Page 688.
 *
 * <p>Cases handled:
 * <ul>
 *   <li>Bi-Directional present → NOT single-direction (existing bi-directional flow applies).</li>
 *   <li>Both Customer/Outbound AND Visitor/Inbound present → NOT single-direction.</li>
 *   <li>ONLY Customer/Outbound present → single-direction; missing = Visitor/Inbound.</li>
 *   <li>ONLY Visitor/Inbound present → single-direction; missing = Customer/Outbound.</li>
 * </ul>
 */
public class SendOrPaySingleDirectionHelper {

    public static final String TRIGGER_CALC_TYPE   = "Send or Pay Financial (All Services)";
    public static final String DIR_CUSTOMER_OUTBOUND = "Customer/Outbound";
    public static final String DIR_VISITOR_INBOUND   = "Visitor/Inbound";
    public static final String DIR_BIDIRECTIONAL     = "Bi-Directional";

    /**
     * Returns {@code true} when the Send or Pay Financial condition is active and exactly one
     * of the two directional values is present (not Bi-Directional, not both).
     */
    public static boolean isSingleDirectionCase(List<Map<String, String>> paramRows) {
        if (paramRows == null || paramRows.isEmpty()) return false;

        Set<String> directions = paramRows.stream()
                .filter(row -> TRIGGER_CALC_TYPE.equals(row.get("Calculation Type")))
                .map(row -> row.getOrDefault("Discount Direction", ""))
                .collect(Collectors.toSet());

        if (directions.isEmpty()) return false;
        if (directions.contains(DIR_BIDIRECTIONAL)) return false;
        if (directions.contains(DIR_CUSTOMER_OUTBOUND) && directions.contains(DIR_VISITOR_INBOUND)) return false;

        return directions.contains(DIR_CUSTOMER_OUTBOUND) || directions.contains(DIR_VISITOR_INBOUND);
    }

    /**
     * Returns the direction that IS present in the Discount Parameter for the Send or Pay
     * Financial trigger rows, or {@code null} if this is not a single-direction case.
     */
    public static String getPresentDirection(List<Map<String, String>> paramRows) {
        if (!isSingleDirectionCase(paramRows)) return null;

        return paramRows.stream()
                .filter(row -> TRIGGER_CALC_TYPE.equals(row.get("Calculation Type")))
                .map(row -> row.getOrDefault("Discount Direction", ""))
                .filter(d -> DIR_CUSTOMER_OUTBOUND.equals(d) || DIR_VISITOR_INBOUND.equals(d))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the direction that is MISSING from the Discount Parameter for the Send or Pay
     * Financial trigger rows, or {@code null} if this is not a single-direction case.
     */
    public static String getMissingDirection(List<Map<String, String>> paramRows) {
        String present = getPresentDirection(paramRows);
        if (present == null) return null;
        return DIR_CUSTOMER_OUTBOUND.equals(present) ? DIR_VISITOR_INBOUND : DIR_CUSTOMER_OUTBOUND;
    }
}
