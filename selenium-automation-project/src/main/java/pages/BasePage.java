package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }
    
    public String getPageTitle() {
        return driver.getTitle();
    }
    
    public void jsClick(WebElement element) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public void waitForVisibility(By locator) {
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitForClickable(By locator) {
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(locator));
    }

    public void scrollToElement(WebElement element) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void saveHtml(String fileName) {
        try {
            java.io.FileWriter writer = new java.io.FileWriter("target/" + fileName);
            writer.write(driver.getPageSource());
            writer.close();
            System.out.println("HTML saved to target/" + fileName);
        } catch (java.io.IOException e) {
            System.err.println("Failed to save HTML: " + e.getMessage());
        }
    }
}
