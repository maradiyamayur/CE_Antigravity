package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class IotronDashboardPage extends BasePage {

    // Locators
    private By agreementCaptureMenuNode = By.xpath(
            "//li[.//span[contains(@class, 'a-TreeView-label') and text()='Agreement Capture']]//div[contains(@class, 'a-TreeView-content')]");
    private By discountAgreementsLink = By
            .xpath("//a[contains(@class, 'a-TreeView-label') and text()='Discount Agreements']");

    // Locators for Discount Agreements Search Page
    private By searchTypeDropdown = By.id("P301_SEARCH_ON"); // Needs verification, guessing common APEX ID structure
    private By searchInput = By.id("P301_SEARCH_STRING"); // Needs verification
    private By searchButton = By.id("B_SEARCH"); // Needs verification

    // Step 7 Locators
    private By forecastingMenuNode = By.xpath(
            "//li[.//span[contains(@class, 'a-TreeView-label') and (normalize-space(text())='Forcasting' or normalize-space(text())='Forecasting')]]//div[contains(@class, 'a-TreeView-content')]");
    private By forecastReportLink = By
            .xpath("//a[contains(@class, 'a-TreeView-label') and contains(., 'Forecast Report')]");

    public IotronDashboardPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToDiscountAgreements() {
        // Wait for the side menu Agreement Capture item
        WebElement menuNode = wait.until(ExpectedConditions.presenceOfElementLocated(agreementCaptureMenuNode));

        // Expand the menu
        try {
            wait.until(ExpectedConditions.elementToBeClickable(menuNode)).click();
        } catch (Exception e) {
            // Fallback JS click if obscured by other elements
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuNode);
        }

        // Wait for Discount Agreements to appear and click
        WebElement discountLink = wait.until(ExpectedConditions.elementToBeClickable(discountAgreementsLink));

        try {
            discountLink.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", discountLink);
        }

        // ── Synchronization guard ────────────────────────────────────────
        // Wait for the P301 page to fully load before returning.
        // We use the Interactive Report container (#p301ir1) as the sentinel
        // because it is one of the first stable elements APEX renders on P301.
        // Without this, selectAllAgreements() arrives before the DOM is ready.
        org.openqa.selenium.support.ui.WebDriverWait pageWait = new org.openqa.selenium.support.ui.WebDriverWait(driver,
                java.time.Duration.ofSeconds(60));
        try {
            pageWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("p301ir1")));
            System.out.println("       P301 Discount Agreements page confirmed loaded (IR region found).");
        } catch (Exception e) {
            // IR region id may differ; fall back to waiting for the radio-button group
            // itself
            System.out.println("       IR region #p301ir1 not found; falling back to radio-button sentinel...");
            pageWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[id^='P301_SHOW_LIVE_AGRMTS']")));
            System.out.println("       P301 radio-button group confirmed present.");
        }
        // Brief stabilisation pause for APEX JS to finish post-load binding
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public void navigateToForecastReportPerAgreement() {
        System.out.println("Navigating to Forecast Report per Agreement...");

        // 1. Expand Forcasting
        System.out.println("Expanding Forcasting node...");
        WebElement forecastNode = wait.until(ExpectedConditions.presenceOfElementLocated(forecastingMenuNode));
        expandNode(forecastNode);

        // 2. Click Forecast Report per Agreement
        System.out.println("Clicking Forecast Report per Agreement...");
        WebElement forecastLink = wait.until(ExpectedConditions.presenceOfElementLocated(forecastReportLink));
        scrollToElement(forecastLink);
        clickElement(forecastLink);
        System.out.println("Forecast Report link clicked.");
    }

    private void expandNode(WebElement element) {
        // Use JS to expand the node specifically for APEX TreeView
        ((JavascriptExecutor) driver).executeScript(
                "var node = $(arguments[0]).closest('.a-TreeView-node');" +
                        "try {" +
                        "  apex.region('t_TreeNav').widget().treeView('expand', node);" +
                        "} catch (e) {" +
                        "  $('#t_TreeNav').treeView('expand', node);" +
                        "}",
                element);

        // Brief wait for animation/loading
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    private void clickElement(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
}
