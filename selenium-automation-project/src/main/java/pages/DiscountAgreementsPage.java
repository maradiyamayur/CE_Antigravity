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

    /** Reset button – clears the IR search/filter state */
    private By resetButton        = By.xpath("//button[@id='P301_RESET_B']");

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
     * Resets the Interactive Report via the Action menu — three-step flow:
     *
     * <ol>
     *   <li>Wait for the Actions menu button to be CLICKABLE, then click it
     *       (XPath: {@code //button[@id='p301ir1_actions_button']})</li>
     *   <li>Wait for the Reset menu option to be CLICKABLE, then click it
     *       (XPath: {@code //button[@id='p301ir1_actions_menu_10i']})</li>
     *   <li>Wait for the Apply confirmation popup button to be VISIBLE, then
     *       click it (XPath: {@code //button[@class='ui-button--hot ui-button
     *       ui-corner-all ui-widget']})</li>
     * </ol>
     *
     * After clicking Apply this method waits for the IR data panel to finish
     * reloading before returning, so the next iteration can start cleanly.
     *
     * Call this at the START of every loop iteration (before searching) to
     * guarantee the search field is fully cleared from the previous run.
     */
    public void resetViaActionMenu() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(60));

        // ── STEP 1: Open the Actions menu ────────────────────────────────────
        By actionsBtn = By.xpath("//button[@id='p301ir1_actions_button']");
        System.out.println("[Reset] Step 1 – Waiting for Actions menu button to be clickable...");
        WebElement actionsBtnEl = longWait.until(ExpectedConditions.elementToBeClickable(actionsBtn));
        actionsBtnEl.click();
        System.out.println("[Reset] Step 1 – Actions menu opened.");

        // ── STEP 2: Click the "Reset" option from the Actions menu ───────────
        By resetMenuItem = By.xpath("//button[@id='p301ir1_actions_menu_10i']");
        System.out.println("[Reset] Step 2 – Waiting for Reset menu option to be clickable...");
        WebElement resetMenuEl = longWait.until(ExpectedConditions.elementToBeClickable(resetMenuItem));
        resetMenuEl.click();
        System.out.println("[Reset] Step 2 – Reset menu option clicked.");

        // ── STEP 3: Click "Apply" on the confirmation popup ──────────────────
        By applyPopupBtn = By.xpath(
                "//button[@class='ui-button--hot ui-button ui-corner-all ui-widget']");
        System.out.println("[Reset] Step 3 – Waiting for Apply popup button to be visible...");
        WebElement applyBtn = longWait.until(ExpectedConditions.visibilityOfElementLocated(applyPopupBtn));
        applyBtn.click();
        System.out.println("[Reset] Step 3 – Apply popup button clicked. Waiting for page/report to reload...");

        // ── Wait for the IR data panel to finish reloading after Apply ────────
        try {
            longWait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("p301ir1_data_panel"), "Agreements data is loading"));
        } catch (Exception e) {
            // Reload was too fast – the loading message may never appear; continue
        }
        longWait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(
                By.id("p301ir1_data_panel"), "Agreements data is loading")));

        System.out.println("[Reset] Report reset via Action menu completed. Page fully reloaded.");
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

        // Wait briefly for menu items to fully render
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // 2. Try multiple possible column label variants used across IOTRON versions
        String[] columnLabels = {"Agreement", "Agreement Reference", "Agreement Name", "All Columns"};
        boolean columnSelected = false;
        for (String label : columnLabels) {
            try {
                By option = By.xpath(
                    "//li[contains(@class,'a-Menu-item')]//button[normalize-space(.)='" + label + "']");
                WebElement btn = driver.findElement(option);
                if (btn.isDisplayed() && btn.isEnabled()) {
                    btn.click();
                    System.out.println("Column option selected: '" + label + "'");
                    columnSelected = true;
                    break;
                }
            } catch (Exception ignored) {}
        }
        if (!columnSelected) {
            System.out.println("No specific column matched — proceeding with default search column.");
            // Press Escape to close menu and continue with default
            try {
                driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
            } catch (Exception ignored) {}
        }

        // 3. Type the agreement name into the search field
        System.out.println("Entering search term: " + agreementName);
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(searchField));
        // Triple-clear: ensures previous agreement name does not persist on iteration 2+
        searchBox.clear();
        searchBox.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
        searchBox.sendKeys(org.openqa.selenium.Keys.DELETE);
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
     * Clicks the Edit link in the row that contains {@code agreementName}
     * and waits until the Agreement detail page (Page 119) has fully loaded
     * before returning.  The caller can safely call
     * {@link AgreementDetailPage#clickSettlementTab()} immediately after this.
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

        // ── Wait for the Agreement detail page to fully load ─────────────────
        // We wait for the tab-bar container on Page 119.  This element is only
        // present once the browser has navigated away from Page 301, so it acts
        // as a reliable "page-ready" sentinel before any tab interaction.
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        By detailPageTabBar = By.id("SR_P119_tabs");           // outer tab container on page 119
        By fallbackTabBar   = By.id("SR_P119_SETTLEMENT_R_tab"); // the Settlement tab itself
        System.out.println("[clickEditForAgreement] Waiting for Agreement detail page to load...");
        try {
            longWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(detailPageTabBar),
                ExpectedConditions.presenceOfElementLocated(fallbackTabBar)
            ));
            System.out.println("[clickEditForAgreement] Agreement detail page loaded.");
        } catch (Exception e) {
            System.out.println("[clickEditForAgreement] WARNING: Detail page load sentinel not found within 60 s. Proceeding anyway.");
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
