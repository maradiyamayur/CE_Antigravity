package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties = new Properties();
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Could not read config.properties file: " + e.getMessage());
            properties = new Properties();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getUsername() {
        return getProperty("iotron.username");
    }

    public static String getPassword() {
        return getProperty("iotron.password");
    }

    public static String getBaseUrl() {
        return getProperty("iotron.base_url");
    }
}
