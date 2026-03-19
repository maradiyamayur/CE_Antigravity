package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ForecastReportPage extends BasePage {

    // Locators based on target/forecast_report_page.html
    private By allAgreementsRadio = By.id("P639_SHOW_LIVE_AGRMTS_1");
    private By allAgreementsLabel = By.cssSelector("label[for='P639_SHOW_LIVE_AGRMTS_1']");
    private By agreementLovButton = By.id("P639_IOT_AGREEMENT_ID_lov_btn");
    private By refreshReportButton = By.id("REFRESH");
    private By iotCalculationReportButton = By.id("IOT_CALC_REPORT");

    // Popup LOV elements
    private By lovSearchField = By.cssSelector("input.a-PopupLOV-search");
    private By lovSearchButton = By.cssSelector("button.a-PopupLOV-doSearch");
    private By lovDialog = By.cssSelector("div.a-PopupLOV-dialog");

    // Page 688 (Detail Page) elements
    private By detailRefreshButton = By.id("P688_REFRESH");

    // Download Flow Locators
    private By actionsButton = By.cssSelector("button.a-IRR-button--actions");
    private By actionsMenuContainer = By.cssSelector("div.a-Menu[id*='actions_menu']");
    private By downloadMenuOption = By.xpath(
            "//li[contains(@class, 'a-Menu-item')]//button[contains(., 'Download')] | //li[contains(@class, 'a-Menu-item')]//span[contains(text(), 'Download')] | //li[@data-id='download']");
    private By csvOption = By
            .xpath("//li[contains(@class, 'a-IRR-iconList-item')]//span[text()='CSV'] | //li[@data-value='CSV']");
    private By popupDownloadButton = By.xpath("//button[@class='ui-button--hot ui-button ui-corner-all ui-widget']");

    public ForecastReportPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Selects "All" in the Show Agreements filter.
     */
    public void selectAllAgreements() {
        System.out.println("Step 1: Selecting 'Show Agreement All'");
        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(allAgreementsRadio));
        if (!radio.isSelected()) {
            try {
                WebElement label = wait.until(ExpectedConditions.elementToBeClickable(allAgreementsLabel));
                label.click();
            } catch (Exception e) {
                System.out.println("Label click failed, trying JS click on radio button...");
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", radio);
            }
            waitForApexAjax();
        }
        System.out.println("'All' agreements selected: " + radio.isSelected());
    }

    /**
     * Selects an agreement from the Agreement Popup LOV.
     * 
     * @param agreementName The exact name of the agreement to select.
     */
    public void selectAgreement(String agreementName) {
        System.out.println("Starting robust 5-step Agreement selection for: " + agreementName);
        try {
            // 1. Click on Agreement Dropdown
            System.out.println("Step 1: Clicking on Agreement Dropdown...");
            By agreementDropdown = By.xpath("//input[@id='P639_IOT_AGREEMENT_ID']");
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(agreementDropdown));
            
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", dropdown);
            Thread.sleep(1000);
            try {
                dropdown.click();
            } catch (Exception e) {
                new org.openqa.selenium.interactions.Actions(driver).moveToElement(dropdown).click().perform();
            }

            // 2. Wait for Dropdown to Load & 3. Enter Agreement Name in Search
            System.out.println("Step 2 & 3: Waiting for Search field and entering Agreement Name...");
            By searchInputLocator = By.xpath("//input[@aria-label='Search']");
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputLocator));
            
            searchInput.click();
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(agreementName);
            
            // Verify value was entered
            String enteredVal = searchInput.getAttribute("value");
            System.out.println("Search field value: '" + enteredVal + "'");
            if (enteredVal == null || enteredVal.isEmpty()) {
                System.out.println("Retrying sending keys via Javascript...");
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = '" + agreementName + "';", searchInput);
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", searchInput);
            }

            // 4. Click on Search Icon
            System.out.println("Step 4: Clicking on Search Icon...");
            By searchIconLocator = By.xpath("//button[@aria-label='Search']//span[@class='a-Icon icon-search']");
            By searchButtonLocator = By.xpath("//button[@aria-label='Search']");
            
            try {
                WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(searchButtonLocator));
                searchButton.click();
            } catch (Exception e) {
                WebElement searchIcon = wait.until(ExpectedConditions.elementToBeClickable(searchIconLocator));
                searchIcon.click();
            }

            // 5. Select Agreement from Results
            System.out.println("Step 5: Selecting Agreement from Results...");
            // Wait for results to load/refresh - up to 10 seconds for the matching item to appear
            By resultLocator = By.xpath("//li[contains(@class,'a-IconList-item') and contains(normalize-space(.), '" + agreementName + "')]");
            WebElement resultItem = wait.until(ExpectedConditions.elementToBeClickable(resultLocator));
            
            System.out.println("Found match: " + resultItem.getText());
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", resultItem);
            Thread.sleep(500);
            resultItem.click();
            
            System.out.println("Agreement '" + agreementName + "' selected successfully.");

        } catch (Exception e) {
            System.err.println("Failed to select agreement '" + agreementName + "': " + e.getMessage());
            saveHtml("error_forecast_lov_failed.html");
            throw new RuntimeException("Agreement selection failed", e);
        }
    }

    /**
     * Clicks the Refresh Report button.
     */
    public void clickRefreshReport() {
        System.out.println("Refreshing report...");
        wait.until(ExpectedConditions.elementToBeClickable(refreshReportButton)).click();
        waitForApexAjax();
        System.out.println("Report refreshed.");
    }

    /**
     * Clicks the IOT Calculation Report button.
     */
    public void clickIotCalculationReport() {
        System.out.println("Clicking IOT Calculation Report button...");
        wait.until(ExpectedConditions.elementToBeClickable(iotCalculationReportButton)).click();
        waitForApexAjax();
        System.out.println("IOT Calculation Report button clicked.");
    }

    /**
     * Clicks the Refresh button on the IOT Calculation Detail page (Page 688).
     */
    public void clickDetailRefresh() {
        System.out.println("Clicking Detail page Refresh button...");
        wait.until(ExpectedConditions.elementToBeClickable(detailRefreshButton)).click();
        waitForApexAjax();
        System.out.println("Detail page refreshed.");
    }

    /**
     * Clicks the Actions menu button.
     */
    public void clickActionsMenu() {
        System.out.println("Clicking Actions menu...");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(actionsButton));
        scrollToElement(btn);

        // Try JS click first if standard click is known to be problematic
        System.out.println("Using JS click for Actions menu...");
        jsClick(btn);

        // Wait for menu to be populated/visible
        System.out.println("Waiting for Actions menu to become visible...");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(actionsMenuContainer));
            System.out.println("Actions menu container is visible.");
        } catch (Exception e) {
            System.out.println("Menu container not visible after JS click, trying one more standard click...");
            btn.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(actionsMenuContainer));
        }
        waitForApexAjax();
    }

    /**
     * Selects Download from the Actions menu.
     */
    public void selectDownloadFromActions() {
        System.out.println("Selecting 'Download' from Actions menu...");
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(downloadMenuOption));
        try {
            option.click();
        } catch (Exception e) {
            System.out.println("Standard click on Download option failed, trying JS click...");
            jsClick(option);
        }
        waitForApexAjax();
    }

    /**
     * Selects CSV Option from the Download popup.
     */
    public void selectCsvOptionInPopup() {
        System.out.println("Selecting 'CSV' option in Download popup...");
        WebElement opt = wait.until(ExpectedConditions.presenceOfElementLocated(csvOption));
        scrollToElement(opt);
        jsClick(opt);
        waitForApexAjax();
    }

    /**
     * Clicks the Download button in the popup.
     */
    public void clickDownloadInPopup() {
        System.out.println("Clicking 'Download' button in popup...");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(popupDownloadButton));
        jsClick(btn);
        System.out.println("Download button clicked.");

        // Wait for the download to start (standard pause for file generation)
        try {
            System.out.println("Waiting 5s for download to initiate...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper to wait for APEX AJAX calls to finish.
     */
    private void waitForApexAjax() {
        try {
            Thread.sleep(2000); // Initial wait
            // Ideally use a more robust check for APEX loading indicator if available
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }}

    
            
                    
    
        
    
    
    
    
    
    
        
    