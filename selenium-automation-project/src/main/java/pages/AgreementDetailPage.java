package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.HashMap;
import java.util.Map;

public class AgreementDetailPage extends BasePage {

    private By settlementTab = By.id("SR_P119_SETTLEMENT_R_tab");
    private By settlementRegion = By.id("P119_SETTLEMENT_R");
    private By generateSettlementBtn = By.xpath("//button[@id='NEW_SETTLEMENT_STATEMENT']");
    private By administrationTab = By.id("SR_R581712291139259969_tab");
    private By latestDownloadLink = By.xpath("//div[@id='p119reg18_content']//table[contains(@class, 'a-IRR-table')]//tr[td]//a[@download]");

    // Locators for Settlement Parameters (Interactive Grid: rebateparameters)
    private By directionCell = By.xpath("//div[@id='rebateparameters']//td[@data-column='IOT_REBATE_DIRECTION']");
    private By serviceTypeCell = By.xpath("//div[@id='rebateparameters']//td[@data-column='SERVICE_TYPE_NAME']");
    private By eventTypeCell = By.xpath("//div[@id='rebateparameters']//td[@data-column='EVENT_TYPE_NAME']");
    private By basisValueCell = By.xpath("//div[@id='rebateparameters']//td[@data-column='REBATE_BASIS_VALUE']");
    private By taxTypeCell = By.xpath("//div[@id='rebateparameters']//td[@data-column='TAX_TYPE_DESC']");

    public AgreementDetailPage(WebDriver driver) {
        super(driver);
    }

    public void clickSettlementTab() {
        System.out.println("Clicking Settlement Tab...");

        // ── Guard: ensure we are on the Agreement detail page before touching tabs ──
        // Wait for the Settlement tab element to be PRESENT in the DOM first.
        // This handles the race condition where clickSettlementTab() is called
        // while the browser is still navigating from Page 301 to Page 119.
        System.out.println("[clickSettlementTab] Waiting for Settlement tab to be present in DOM...");
        wait.until(ExpectedConditions.presenceOfElementLocated(settlementTab));
        System.out.println("[clickSettlementTab] Settlement tab is present. Waiting for it to be clickable...");

        try {
            WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(settlementTab));
            scrollToElement(tab);
            tab.click();
        } catch (Exception e) {
            System.out.println("Standard click on tab failed or not found, trying JS click on link...");
            try {
                WebElement tabLink = driver.findElement(By.cssSelector("#SR_P119_SETTLEMENT_R_tab a"));
                jsClick(tabLink);
            } catch (Exception ex) {
                saveHtml("error_settlement_tab_not_found.html");
                throw e;
            }
        }
        
        // Wait for either the region or the button to be visible
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(settlementRegion),
                ExpectedConditions.visibilityOfElementLocated(generateSettlementBtn)
            ));
            System.out.println("Settlement Region/Button is now visible.");
        } catch (Exception e) {
            System.out.println("Settlement content did not appear after click.");
            saveHtml("error_settlement_content_missing.html");
            throw e;
        }
    }

    public void clickGenerateSettlement() {
        System.out.println("Clicking GENERATE SETTLEMENT button...");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(generateSettlementBtn));
        scrollToElement(btn);
        try {
            btn.click();
        } catch (Exception e) {
            System.out.println("Standard click failed, trying JS click...");
            jsClick(btn);
        }
        
        // Wait for potential loading or processing
        System.out.println("Waiting for settlement grid to refresh...");
        try {
            // Wait for the 'Processing' overlay to disappear if it appears, or just wait for stabilization
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Map<String, String> getSettlementParameters() {
        System.out.println("Extracting settlement parameters from 'rebateparameters' grid...");
        Map<String, String> params = new HashMap<>();
        
        try {
            params.put("discount direction", wait.until(ExpectedConditions.visibilityOfElementLocated(directionCell)).getText().trim());
            params.put("service type", wait.until(ExpectedConditions.visibilityOfElementLocated(serviceTypeCell)).getText().trim());
            params.put("event type", wait.until(ExpectedConditions.visibilityOfElementLocated(eventTypeCell)).getText().trim());
            params.put("discount basis value", wait.until(ExpectedConditions.visibilityOfElementLocated(basisValueCell)).getText().trim());
            
            String taxType = wait.until(ExpectedConditions.visibilityOfElementLocated(taxTypeCell)).getText().trim();
            params.put("tax type", taxType);
            
            System.out.println("Extraction successful.");
        } catch (Exception e) {
            System.err.println("Failed to extract settlement parameters: " + e.getMessage());
            saveHtml("error_extraction_failed.html");
        }
        
        return params;
    }

    public void clickAdministrationTab() {
        System.out.println("Clicking Administration Tab...");
        WebElement tab = wait.until(ExpectedConditions.presenceOfElementLocated(administrationTab));
        scrollToElement(tab);
        try {
            tab.click();
        } catch (Exception e) {
            jsClick(tab);
        }
        System.out.println("Administration Tab clicked. Waiting for Document Library...");
        // Wait for some element in the administration tab to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("p119reg18")));
    }

    public void downloadLatestReport() {
        System.out.println("Attempting to download the latest report...");
        
        try {
            // Give it a bit more time for the report to refresh
            System.out.println("Wait 3s for report refresh...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            
            WebElement downloadLink = wait.until(ExpectedConditions.elementToBeClickable(latestDownloadLink));
            scrollToElement(downloadLink);
            downloadLink.click();
            System.out.println("Download link clicked.");
        } catch (Exception e) {
            System.out.println("Failed to click download link, trying fallback to any download link in the table...");
            saveHtml("error_download_link_failed.html");
            try {
                WebElement fallbackLink = driver.findElement(By.cssSelector("a[download]"));
                jsClick(fallbackLink);
                System.out.println("JS click on fallback download link succeeded.");
            } catch (Exception ex) {
                System.out.println("Completely failed to find a download link.");
                throw e;
            }
        }
        
        System.out.println("Waiting 5 seconds for download to start...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
