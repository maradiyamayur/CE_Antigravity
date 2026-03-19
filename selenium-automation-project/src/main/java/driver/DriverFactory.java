package driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;

public class DriverFactory {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver initDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        
        // Configure download directory
        String downloadPath = System.getProperty("user.dir") + File.separator + "target" + File.separator + "downloads";
        File downloadFolder = new File(downloadPath);
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }
        
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadPath);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.extensions_to_open", "applications/pdf");
        prefs.put("safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", prefs);

        WebDriver webDriver = new ChromeDriver(options);
        
        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        driver.set(webDriver);
        return getDriver();
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    private static boolean shouldQuit = true;
    
    public static void setShouldQuit(boolean quit) {
        shouldQuit = quit;
    }

    public static void quitDriver() {
        if (shouldQuit && driver.get() != null) {
            driver.get().quit();
            driver.remove();
        } else if (!shouldQuit && driver.get() != null) {
            System.out.println("Browser kept open as per configuration.");
        }
    }
}
