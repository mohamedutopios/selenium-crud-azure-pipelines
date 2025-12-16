package com.example.demo;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;




@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumEndToEndTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    private static boolean isCI;

    // ==================================================
    // SETUP GLOBAL
    // ==================================================
    @BeforeAll
    static void setupClass() {
        isCI = System.getenv("CI") != null || System.getenv("AGENT_NAME") != null;
        System.out.println(isCI ? "🤖 Mode CI" : "🖥️ Mode local");
    }

    // ==================================================
    // SETUP PAR TEST
    // ==================================================
    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();

        if (isCI) {
            options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080"
            );
        } else {
            options.addArguments("--start-maximized");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ==================================================
    // TESTS
    // ==================================================

    @Test
    @Order(1)
    void testLoginPage() {
        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.titleIs("Login"));
        assertEquals("Login", driver.getTitle());

        assertNotNull(driver.findElement(By.id("username")));
        assertNotNull(driver.findElement(By.id("password")));
        assertNotNull(driver.findElement(By.cssSelector("button[type='submit']")));
    }

    @Test
    @Order(2)
    void testSuccessfulLogin() {
        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")))
                .sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/products"));
        assertTrue(driver.getCurrentUrl().contains("/products"));
    }

    @Test
    @Order(3)
    void testProductListPage() {
        login();

        wait.until(ExpectedConditions.titleIs("Products List"));
        WebElement table = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("products-table"))
        );

        assertNotNull(table);
        List<WebElement> rows =
                table.findElements(By.cssSelector("tbody tr"));
        assertTrue(rows.size() > 0);
    }

    @Test
    @Order(4)
    void testCreateNewProduct() {
        login();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-product-btn")))
                .click();

        wait.until(ExpectedConditions.urlContains("/products/new"));

        String productName = "Test Product " + System.currentTimeMillis();

        driver.findElement(By.id("name")).sendKeys(productName);
        driver.findElement(By.id("description")).sendKeys("This is a test product");
        driver.findElement(By.id("price")).sendKeys("99.99");
        driver.findElement(By.id("quantity")).sendKeys("15");

        driver.findElement(By.id("save-product-btn")).click();

        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));

        assertTrue(driver.getPageSource().contains(productName));
    }

    @Test
    @Order(5)
    void testEditProduct() {
        login();

        WebElement editButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-warning"))
        );
        editButton.click();

        wait.until(ExpectedConditions.urlContains("/products/edit/"));

        String newName = "Updated Product " + System.currentTimeMillis();
        WebElement nameField = driver.findElement(By.id("name"));
        nameField.clear();
        nameField.sendKeys(newName);

        driver.findElement(By.cssSelector("button.btn-primary")).click();

        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));

        assertTrue(driver.getPageSource().contains(newName));
    }

    @Test
    @Order(6)
    void testDeleteProduct() {
        login();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        List<WebElement> rowsBefore =
                driver.findElements(By.cssSelector("#products-table tbody tr"));
        int countBefore = rowsBefore.size();

        driver.findElement(By.cssSelector("button.btn-danger")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        List<WebElement> rowsAfter =
                driver.findElements(By.cssSelector("#products-table tbody tr"));

        assertEquals(countBefore - 1, rowsAfter.size());
    }

    @Test
    @Order(7)
    void testLogout() {
        login();

        driver.findElement(By.cssSelector("button.btn-secondary")).click();
        wait.until(ExpectedConditions.urlContains("/login"));

        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(8)
    void testCompleteWorkflow() {
        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")))
                .sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));

        String productName = "Final Test " + System.currentTimeMillis();

        driver.findElement(By.id("add-product-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")))
                .sendKeys(productName);

        driver.findElement(By.id("description")).sendKeys("Description finale");
        driver.findElement(By.id("price")).sendKeys("149.99");
        driver.findElement(By.id("quantity")).sendKeys("25");

        driver.findElement(By.id("save-product-btn")).click();
        wait.until(ExpectedConditions.urlContains("/products"));

        assertTrue(driver.getPageSource().contains(productName));

        driver.findElement(By.cssSelector("button.btn-secondary")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    // ==================================================
    // MÉTHODES UTILITAIRES
    // ==================================================
    private void login() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")))
                .sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/products"));
    }
}
