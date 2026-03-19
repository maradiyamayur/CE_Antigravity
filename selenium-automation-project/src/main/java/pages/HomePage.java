package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {

    // Locator for the IOTRON link in the top navigation bar
    // Finding the anchor tag that contains a span with text "IOTRON"
    private By iotronLink = By.xpath("//a[descendant::span[contains(text(), 'IOTRON')]]");
    // Alternatively, we could use the icon class: By.cssSelector(".iotron_icon");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void waitForHomePageToLoad() {
        // Wait until the IOTRON link is visible, indicating the top nav bar is loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(iotronLink));
    }

    public void navigateToIotron() {
        wait.until(ExpectedConditions.elementToBeClickable(iotronLink)).click();
    }
}
