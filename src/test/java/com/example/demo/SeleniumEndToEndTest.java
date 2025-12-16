package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("E-Commerce Application")
@Feature("Product Management")
public class SeleniumEndToEndTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        
        boolean isCI = System.getenv("CI") != null || 
                       System.getenv("AGENT_NAME") != null;
        
        if (isCI) {
            System.out.println("🤖 Mode CI/CD - Configuration headless");
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-notifications");
        } else {
            System.out.println("🖥️  Mode local - Navigateur visible");
            options.addArguments("--start-maximized");
        }
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
        
        System.out.println("✅ WebDriver initialisé - URL: " + baseUrl);
    }

    @AfterEach
    public void teardown(TestInfo testInfo) {
        if (driver != null) {
            // Prendre un screenshot en cas d'échec
            if (testInfo.getTestMethod().isPresent()) {
                takeScreenshot(testInfo.getDisplayName());
            }
            
            driver.quit();
            System.out.println("✅ WebDriver fermé");
        }
    }

    @Test
    @Order(1)
    @Story("Authentication")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Vérifier que la page de login s'affiche correctement")
    public void testLoginPage() {
        step("Naviguer vers la page de login");
        driver.get(baseUrl + "/login");
        
        step("Attendre le chargement de la page");
        wait.until(ExpectedConditions.titleIs("Login"));
        
        step("Vérifier le titre de la page");
        String pageTitle = driver.getTitle();
        assertEquals("Login", pageTitle);

        step("Vérifier la présence des champs de formulaire");
        WebElement usernameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        assertNotNull(usernameField);
        assertNotNull(passwordField);
        assertNotNull(loginButton);
        
        attachScreenshot("Page de login");
        System.out.println("✅ Page de login OK");
    }

    @Test
    @Order(2)
    @Story("Authentication")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Vérifier qu'un utilisateur peut se connecter avec des identifiants valides")
    public void testSuccessfulLogin() {
        step("Naviguer vers la page de login");
        driver.get(baseUrl + "/login");

        step("Remplir le formulaire de connexion");
        WebElement usernameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        usernameField.sendKeys("admin");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("admin");

        attachScreenshot("Formulaire rempli");

        step("Soumettre le formulaire");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        step("Vérifier la redirection vers /products");
        wait.until(ExpectedConditions.urlContains("/products"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/products"));
        
        attachScreenshot("Page après login");
        System.out.println("✅ Login réussi");
    }

    @Test
    @Order(3)
    @Story("Product List")
    @Severity(SeverityLevel.NORMAL)
    @Description("Vérifier que la liste des produits s'affiche correctement")
    public void testProductListPage() {
        login();

        step("Vérifier le titre de la page");
        wait.until(ExpectedConditions.titleIs("Products List"));
        String pageTitle = driver.getTitle();
        assertEquals("Products List", pageTitle);

        step("Vérifier la présence du tableau");
        WebElement table = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("products-table"))
        );
        assertNotNull(table);

        step("Vérifier qu'il y a des produits");
        List<WebElement> rows = driver.findElements(
            By.cssSelector("#products-table tbody tr")
        );
        assertTrue(rows.size() > 0);
        
        attachScreenshot("Liste des produits");
        Allure.addAttachment("Nombre de produits", String.valueOf(rows.size()));
        System.out.println("✅ Liste OK - " + rows.size() + " produit(s)");
    }

    @Test
    @Order(4)
    @Story("Product CRUD")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Vérifier qu'un utilisateur peut créer un nouveau produit")
    public void testCreateNewProduct() {
        login();

        step("Cliquer sur le bouton Add New Product");
        WebElement addButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("add-product-btn"))
        );
        addButton.click();

        step("Vérifier la navigation vers /products/new");
        wait.until(ExpectedConditions.urlContains("/products/new"));
        attachScreenshot("Formulaire de création");

        step("Remplir le formulaire de création");
        String productName = "Test Product " + System.currentTimeMillis();
        
        WebElement nameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("name"))
        );
        nameField.sendKeys(productName);
        
        driver.findElement(By.id("description")).sendKeys("Test description");
        driver.findElement(By.id("price")).sendKeys("99.99");
        driver.findElement(By.id("quantity")).sendKeys("15");

        attachScreenshot("Formulaire rempli");

        step("Soumettre le formulaire");
        WebElement saveButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("save-product-btn"))
        );
        saveButton.click();

        step("Vérifier la redirection et la présence du produit");
        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Test Product"));
        
        attachScreenshot("Produit créé");
        Allure.addAttachment("Nom du produit", productName);
        System.out.println("✅ Produit créé: " + productName);
    }

    @Test
    @Order(5)
    @Story("Product CRUD")
    @Severity(SeverityLevel.NORMAL)
    @Description("Vérifier qu'un utilisateur peut modifier un produit existant")
    public void testEditProduct() {
        login();

        step("Cliquer sur le bouton Edit");
        WebElement editButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-warning"))
        );
        editButton.click();

        step("Attendre la page d'édition");
        wait.until(ExpectedConditions.urlContains("/products/edit/"));
        attachScreenshot("Formulaire d'édition");

        step("Modifier le nom du produit");
        String newName = "Updated Product " + System.currentTimeMillis();
        WebElement nameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("name"))
        );
        nameField.clear();
        nameField.sendKeys(newName);

        step("Soumettre les modifications");
        WebElement updateButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-primary"))
        );
        updateButton.click();

        step("Vérifier que le produit a été modifié");
        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Updated Product"));
        
        attachScreenshot("Produit modifié");
        Allure.addAttachment("Nouveau nom", newName);
        System.out.println("✅ Produit modifié");
    }

    @Test
    @Order(6)
    @Story("Product CRUD")
    @Severity(SeverityLevel.NORMAL)
    @Description("Vérifier qu'un utilisateur peut supprimer un produit")
    public void testDeleteProduct() {
        login();

        step("Compter les produits avant suppression");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        List<WebElement> rowsBefore = driver.findElements(
            By.cssSelector("#products-table tbody tr")
        );
        int countBefore = rowsBefore.size();
        Allure.addAttachment("Nombre avant", String.valueOf(countBefore));

        step("Cliquer sur le bouton Delete");
        WebElement deleteButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-danger"))
        );
        deleteButton.click();

        step("Accepter la confirmation");
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        step("Vérifier que le produit a été supprimé");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        
        wait.until(driver2 -> {
            List<WebElement> rows = driver.findElements(
                By.cssSelector("#products-table tbody tr")
            );
            return rows.size() == countBefore - 1;
        });

        List<WebElement> rowsAfter = driver.findElements(
            By.cssSelector("#products-table tbody tr")
        );
        int countAfter = rowsAfter.size();
        
        assertEquals(countBefore - 1, countAfter);
        
        attachScreenshot("Après suppression");
        Allure.addAttachment("Nombre après", String.valueOf(countAfter));
        System.out.println("✅ Produit supprimé");
    }

    @Test
    @Order(7)
    @Story("Authentication")
    @Severity(SeverityLevel.NORMAL)
    @Description("Vérifier qu'un utilisateur peut se déconnecter")
    public void testLogout() {
        login();

        step("Cliquer sur le bouton Logout");
        WebElement logoutButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-secondary"))
        );
        logoutButton.click();

        step("Vérifier la redirection vers /login");
        wait.until(ExpectedConditions.urlContains("/login"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"));
        
        attachScreenshot("Après logout");
        System.out.println("✅ Logout réussi");
    }

    @Test
    @Order(8)
    @Story("Complete Workflow")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Tester le workflow complet de bout en bout")
    public void testCompleteWorkflow() {
        System.out.println("\n[TEST COMPLET] 🎯");
        
        step("1. Login");
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")))
            .sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        attachScreenshot("Après login");

        step("2. Voir la liste");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        attachScreenshot("Liste des produits");

        step("3. Créer un produit");
        String productName = "Final Test " + System.currentTimeMillis();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-product-btn"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")))
            .sendKeys(productName);
        driver.findElement(By.id("description")).sendKeys("Description test");
        driver.findElement(By.id("price")).sendKeys("149.99");
        driver.findElement(By.id("quantity")).sendKeys("25");
        attachScreenshot("Formulaire création");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("save-product-btn"))).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        attachScreenshot("Produit créé");

        step("4. Vérifier le produit");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Final Test"));
        Allure.addAttachment("Nom produit", productName);

        step("5. Logout");
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-secondary")
        )).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        attachScreenshot("Après logout");
        
        System.out.println("✅ Workflow complet OK");
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================
    
    @Step("{stepDescription}")
    private void step(String stepDescription) {
        System.out.println("  → " + stepDescription);
    }

    private void login() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")))
            .sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin");
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")
        )).click();
        wait.until(ExpectedConditions.urlContains("/products"));
    }

    @Attachment(value = "Screenshot: {name}", type = "image/png")
    private void attachScreenshot(String name) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", 
                new java.io.ByteArrayInputStream(screenshot), "png");
        } catch (Exception e) {
            System.err.println("Erreur screenshot: " + e.getMessage());
        }
    }

    private void takeScreenshot(String testName) {
        try {
            // Créer le dossier screenshots
            Path screenshotDir = Paths.get("target/screenshots");
            Files.createDirectories(screenshotDir);
            
            // Prendre le screenshot
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            
            // Sauvegarder
            String fileName = testName.replaceAll("[^a-zA-Z0-9]", "_") + ".png";
            Path destination = screenshotDir.resolve(fileName);
            Files.copy(source.toPath(), destination);
            
            System.out.println("📸 Screenshot sauvegardé: " + destination);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde screenshot: " + e.getMessage());
        }
    }
}