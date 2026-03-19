# Selenium Automation Project

A basic Java Selenium automation project using Maven, TestNG, and WebDriverManager.

## Tech Stack
- Java 17
- Selenium WebDriver 4.x
- TestNG
- WebDriverManager

## Project Structure
- `src/main/java/driver/DriverFactory.java`: Initializes and manages `ChromeDriver` using `WebDriverManager`.
- `src/main/java/pages/BasePage.java`: Contains common Selenium wrapper methods.
- `src/test/java/tests/SampleTest.java`: A TestNG sample test that launches Chrome, opens Google, and verifies the title.
- `resources/config.properties`: A configuration properties file.

## Run Tests
Navigate to the project directory and run the following command to execute the tests:

```sh
mvn clean test
```
