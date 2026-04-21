package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Persists and reloads settlement sheet data to/from a JSON file on disk.
 *
 * <p>The JSON structure written to disk (one file per agreement):
 * <pre>
 * {
 *   "agreementName": "Orange Group MAR25 FEB26",
 *   "settlementSummary": [
 *     { "Header1": "value", "Header2": "value", ... },
 *     ...
 *   ],
 *   "discountParameter": [
 *     { "Header1": "value", ... },
 *     ...
 *   ]
 * }
 * </pre>
 *
 * <p>File location: {@code target/settlement_data_<AgreementName>.json}
 *
 * <p>Usage:
 * <pre>
 *   // Save
 *   SettlementDataStore.save(agreementName, summaryRows, paramRows, outputPath);
 *
 *   // Load later
 *   SettlementDataStore.SettlementData data = SettlementDataStore.load(outputPath);
 *   List&lt;Map&lt;String, String&gt;&gt; summary = data.getSettlementSummary();
 *   List&lt;Map&lt;String, String&gt;&gt; params  = data.getDiscountParameter();
 * </pre>
 */
public class SettlementDataStore {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Serializes both settlement sheets to a pretty-printed JSON file.
     *
     * @param agreementName  Name of the agreement being processed.
     * @param summaryRows    Rows from the "Settlement Summary" sheet.
     * @param paramRows      Rows from the "Discount Parameter" sheet.
     * @param outputFilePath Absolute path where the JSON file will be written.
     */
    public static void save(String agreementName,
                            List<Map<String, String>> summaryRows,
                            List<Map<String, String>> paramRows,
                            String outputFilePath) {
        SettlementData data = new SettlementData();
        data.setAgreementName(agreementName);
        data.setSettlementSummary(summaryRows);
        data.setDiscountParameter(paramRows);

        try {
            File outFile = new File(outputFilePath);
            // Ensure parent directory exists
            outFile.getParentFile().mkdirs();
            MAPPER.writeValue(outFile, data);
            System.out.println("[SettlementDataStore] Saved " + summaryRows.size()
                    + " Settlement Summary rows and " + paramRows.size()
                    + " Discount Parameter rows to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("[SettlementDataStore] Failed to save settlement data: " + e.getMessage());
        }
    }

    /**
     * Loads previously persisted settlement data from a JSON file.
     *
     * @param filePath Absolute path to the JSON file created by {@link #save}.
     * @return Deserialized {@link SettlementData} object, or {@code null} on error.
     */
    public static SettlementData load(String filePath) {
        try {
            SettlementData data = MAPPER.readValue(new File(filePath), SettlementData.class);
            System.out.println("[SettlementDataStore] Loaded settlement data for agreement: "
                    + data.getAgreementName());
            return data;
        } catch (IOException e) {
            System.err.println("[SettlementDataStore] Failed to load settlement data from: "
                    + filePath + " — " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds the standard output file path for a given agreement name.
     * Sanitizes the agreement name so it is safe for use as a filename.
     *
     * @param agreementName  Raw agreement name (e.g. "Orange Group MAR25 FEB26").
     * @param baseOutputDir  Base directory (e.g. {@code System.getProperty("user.dir") + "/target"}).
     * @return Absolute path string like {@code <baseOutputDir>/settlement_data_Orange_Group_MAR25_FEB26.json}.
     */
    public static String buildOutputPath(String agreementName, String baseOutputDir) {
        String safeName = agreementName.replaceAll("[^a-zA-Z0-9]", "_");
        return baseOutputDir + File.separator + "settlement_data_" + safeName + ".json";
    }

    // ------------------------------------------------------------------
    // Data model
    // ------------------------------------------------------------------

    /**
     * Simple POJO representing the settlement data for one agreement.
     * Jackson requires a no-arg constructor and getters/setters for serialization.
     */
    public static class SettlementData {

        private String agreementName;
        private List<Map<String, String>> settlementSummary;
        private List<Map<String, String>> discountParameter;

        public SettlementData() {}

        public String getAgreementName() { return agreementName; }
        public void setAgreementName(String agreementName) { this.agreementName = agreementName; }

        public List<Map<String, String>> getSettlementSummary() { return settlementSummary; }
        public void setSettlementSummary(List<Map<String, String>> settlementSummary) {
            this.settlementSummary = settlementSummary;
        }

        public List<Map<String, String>> getDiscountParameter() { return discountParameter; }
        public void setDiscountParameter(List<Map<String, String>> discountParameter) {
            this.discountParameter = discountParameter;
        }
    }
}
