package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DiscountAgreementsPage extends BasePage {

    // -----------------------------------------------------------------------
    // Locators – Discount Agreements list page (Page 301)
    // -----------------------------------------------------------------------

    /** "Show Agreements" radio: All */
    private By allAgreementsRadio = By.id("P301_SHOW_LIVE_AGRMTS_1");
    private By applyButton       = By.id("P86_APPLY_B");

    /** Interactive Report search controls */
    private By searchField        = By.id("p301ir1_search_field");
    private By searchButton       = By.id("p301ir1_search_button");
    private By colSelectorButton  = By.id("p301ir1_column_search_root");  // column-selector icon

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public DiscountAgreementsPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------------
    // Public methods
    // -----------------------------------------------------------------------

    /**
     * Selects "All" in the "Show Agreements" radio group and waits for the
     * list to reload.
     */
    public void selectAllAgreements() {
        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(allAgreementsRadio));
        try {
            radio.click();
        } catch (Exception e) {
            driver.findElement(By.cssSelector("label[for='P301_SHOW_LIVE_AGRMTS_1']")).click();
        }

        try {
            WebElement apply = wait.until(ExpectedConditions.elementToBeClickable(applyButton));
            apply.click();
        } catch (Exception e) {
            // May already be applied or may be AJAX-only
        }

        // Wait for data to reload
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        try {
            longWait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("p301ir1_data_panel"), "Agreements data is loading"));
        } catch (Exception e) {
            // Reload was too fast – continue
        }
        longWait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(
                By.id("p301ir1_data_panel"), "Agreements data is loading")));

        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    /**
     * Sets the search filter type to "Agreement" (column selector), types the
     * agreement name, presses Search, and waits for the result row to appear.
     *
     * @param agreementName exact or partial text to search for, e.g. "Transatel 2025 - 2030"
     */
    public void searchByAgreementType(String agreementName) {
        System.out.println("Setting search column to 'Agreement'...");

        // 1. Open the column-selector menu
        wait.until(ExpectedConditions.elementToBeClickable(colSelectorButton)).click();

        // 2. Choose "Agreement" (the column that holds the agreement reference/name)
        //    The visible option label in APEX IRR column-selector is "Agreement"
        By agreementColumnOption = By.xpath(
                "//li[contains(@class,'a-Menu-item')]//button[normalize-space(text())='Agreement']");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(agreementColumnOption)).click();
            System.out.println("'Agreement' column option selected.");
        } catch (Exception e) {
            System.out.println("Could not find 'Agreement' column option, trying 'Agreement Reference'...");
            By agreementRefOption = By.xpath(
                "//li[contains(@class,'a-Menu-item')]//button[contains(.,'Agreement Reference')]");
            wait.until(ExpectedConditions.elementToBeClickable(agreementRefOption)).click();
        }

        // 3. Type the agreement name into the search field
        System.out.println("Entering search term: " + agreementName);
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(searchField));
        searchBox.clear();
        searchBox.sendKeys(agreementName);

        // 4. Click the Search button
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();

        // 5. Wait for the matching row to appear
        waitForSearchResults(agreementName);
    }

    /**
     * Legacy overload kept for backward compatibility – delegates to
     * {@link #searchByAgreementType(String)}.
     */
    public void searchAgreement(String agreementName) {
        searchByAgreementType(agreementName);
    }

    /**
     * Clicks the Edit link in the row that contains {@code agreementName}.
     */
    public void clickEditForAgreement(String agreementName) {
        System.out.println("Clicking Edit for agreement: " + agreementName);
        By editLinkLocator = By.xpath(
            "//tr[td[contains(., '" + agreementName + "')]]" +
            "//td[contains(@class,'a-IRR-linkCol')]//a");
        WebElement editLink = wait.until(ExpectedConditions.elementToBeClickable(editLinkLocator));
        scrollToElement(editLink);
        try {
            editLink.click();
        } catch (Exception e) {
            jsClick(editLink);
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void waitForSearchResults(String agreementName) {
        // Small pause so the loading indicator has a chance to appear
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Wait for any loading indicator to go away
        By loadingMsg = By.xpath("//span[contains(@class,'a-IRR-noDataMsg-text')]");
        try {
            wait.until(ExpectedConditions.invisibilityOfElementWithText(
                    loadingMsg, "Agreements data is loading"));
        } catch (Exception e) {
            // Already gone or never appeared
        }

        // Wait for the result row
        By rowLocator = By.xpath(
            "//table[contains(@class,'a-IRR-table')]//td[contains(.,'" + agreementName + "')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(rowLocator));
        System.out.println("Search result found for: " + agreementName);
    }
}
