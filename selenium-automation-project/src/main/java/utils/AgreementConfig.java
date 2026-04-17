package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the agreements configuration from {@code agreements.json} on the
 * classpath and returns only the agreements that are marked as enabled.
 *
 * <p>JSON format expected:
 * <pre>
 * {
 *   "agreements": [
 *     { "AGREEMENT_NAME": "Orange Group:MAR25 FEB26", "enabled": true  },
 *     { "AGREEMENT_NAME": "AG67890",                  "enabled": false }
 *   ]
 * }
 * </pre>
 *
 * <p>Only entries with {@code "enabled": true} are returned.
 * This class does NOT modify any existing test logic.
 */
public class AgreementConfig {

    private static final String CONFIG_FILE = "agreements.json";

    /**
     * Loads {@code agreements.json} from the classpath and returns the list of
     * {@code AGREEMENT_NAME} values whose {@code enabled} flag is {@code true}.
     *
     * @return ordered list of enabled agreement names; never {@code null}
     * @throws Exception if the file is missing or cannot be parsed
     */
    public static List<String> loadEnabledAgreements() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream is = AgreementConfig.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE);

        if (is == null) {
            throw new Exception("[AgreementConfig] '" + CONFIG_FILE
                    + "' not found on classpath. "
                    + "Make sure it is in src/test/resources/.");
        }

        JsonNode root  = mapper.readTree(is);
        JsonNode items = root.path("agreements");

        List<String> enabledNames = new ArrayList<>();

        if (items.isArray()) {
            for (JsonNode node : items) {
                boolean enabled = node.path("enabled").asBoolean(false);
                if (enabled) {
                    String name = node.path("AGREEMENT_NAME").asText("").trim();
                    if (!name.isEmpty()) {
                        enabledNames.add(name);
                    }
                }
            }
        }

        System.out.println("[AgreementConfig] Loaded " + enabledNames.size()
                + " enabled agreement(s) from " + CONFIG_FILE + ": " + enabledNames);

        return enabledNames;
    }
}
