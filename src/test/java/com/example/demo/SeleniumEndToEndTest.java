package com.example.demo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        // Retirer headless pour voir le navigateur
        // options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
        
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginPage() throws InterruptedException {
        driver.get(baseUrl + "/login");
        Thread.sleep(1000); // Pour voir la page

        String pageTitle = driver.getTitle();
        assertEquals("Login", pageTitle);

        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        assertNotNull(usernameField);
        assertNotNull(passwordField);
        assertNotNull(loginButton);
        
        Thread.sleep(1000); // Pour voir la page
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() throws InterruptedException {
        driver.get(baseUrl + "/login");
        Thread.sleep(1000);

        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));

        usernameField.sendKeys("admin");
        Thread.sleep(500);
        
        passwordField.sendKeys("admin");
        Thread.sleep(500);

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Attendre la redirection vers /products
        wait.until(ExpectedConditions.urlContains("/products"));
        Thread.sleep(1000);

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/products"));
    }

    @Test
    @Order(3)
    public void testProductListPage() throws InterruptedException {
        // Login d'abord
        login();
        Thread.sleep(1000);

        // Vérifier que nous sommes sur la page des produits
        String pageTitle = driver.getTitle();
        assertEquals("Products List", pageTitle);

        // Vérifier la présence du tableau
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        assertNotNull(table);
        Thread.sleep(1000);

        // Vérifier qu'il y a des produits
        List<WebElement> rows = driver.findElements(By.cssSelector("#products-table tbody tr"));
        assertTrue(rows.size() > 0, "Il devrait y avoir des produits dans la liste");
        
        Thread.sleep(1000);
    }

    @Test
    @Order(4)
    public void testCreateNewProduct() throws InterruptedException {
        login();
        Thread.sleep(1000);

        // Cliquer sur "Add New Product"
        WebElement addButton = driver.findElement(By.id("add-product-btn"));
        addButton.click();
        Thread.sleep(1000);

        // Attendre d'être sur la page de création
        wait.until(ExpectedConditions.urlContains("/products/new"));

        // Remplir le formulaire
        WebElement nameField = driver.findElement(By.id("name"));
        WebElement descriptionField = driver.findElement(By.id("description"));
        WebElement priceField = driver.findElement(By.id("price"));
        WebElement quantityField = driver.findElement(By.id("quantity"));

        nameField.sendKeys("Test Product");
        Thread.sleep(500);
        
        descriptionField.sendKeys("This is a test product");
        Thread.sleep(500);
        
        priceField.sendKeys("99.99");
        Thread.sleep(500);
        
        quantityField.sendKeys("15");
        Thread.sleep(1000);

        // Soumettre le formulaire
        WebElement saveButton = driver.findElement(By.id("save-product-btn"));
        saveButton.click();

        // Attendre la redirection vers la liste
        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        Thread.sleep(1000);

        // Vérifier que le produit apparaît dans la liste
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Test Product"));
        
        Thread.sleep(1000);
    }

    @Test
    @Order(5)
    public void testEditProduct() throws InterruptedException {
        login();
        Thread.sleep(1000);

        // Trouver le premier bouton Edit
        WebElement editButton = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn-warning"))
        );
        editButton.click();
        Thread.sleep(1000);

        // Attendre d'être sur la page d'édition
        wait.until(ExpectedConditions.urlContains("/products/edit/"));

        // Modifier le nom
        WebElement nameField = driver.findElement(By.id("name"));
        nameField.clear();
        Thread.sleep(500);
        
        nameField.sendKeys("Updated Product");
        Thread.sleep(1000);

        // Soumettre
        WebElement updateButton = driver.findElement(By.cssSelector("button.btn-primary"));
        updateButton.click();

        // Attendre la redirection
        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        Thread.sleep(1000);

        // Vérifier que le produit a été modifié
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Updated Product"));
        
        Thread.sleep(1000);
    }

    @Test
    @Order(6)
    public void testDeleteProduct() throws InterruptedException {
        login();
        Thread.sleep(1000);

        // Compter le nombre de produits avant suppression
        List<WebElement> rowsBefore = driver.findElements(By.cssSelector("#products-table tbody tr"));
        int countBefore = rowsBefore.size();
        Thread.sleep(500);

        // Trouver et cliquer sur le premier bouton Delete
        WebElement deleteButton = driver.findElement(By.cssSelector("button.btn-danger"));
        deleteButton.click();
        Thread.sleep(500);

        // Accepter l'alerte de confirmation
        driver.switchTo().alert().accept();
        Thread.sleep(1000);

        // Attendre le rechargement de la page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        Thread.sleep(1000);

        // Vérifier que le nombre de produits a diminué
        List<WebElement> rowsAfter = driver.findElements(By.cssSelector("#products-table tbody tr"));
        int countAfter = rowsAfter.size();
        
        assertEquals(countBefore - 1, countAfter, "Le nombre de produits devrait avoir diminué de 1");
        
        Thread.sleep(1000);
    }

    @Test
    @Order(7)
    public void testLogout() throws InterruptedException {
        login();
        Thread.sleep(1000);

        // Cliquer sur Logout
        WebElement logoutButton = driver.findElement(By.cssSelector("button.btn-secondary"));
        logoutButton.click();
        Thread.sleep(1000);

        // Attendre d'être redirigé vers la page de login
        wait.until(ExpectedConditions.urlContains("/login"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"));
        
        Thread.sleep(1000);
    }

    @Test
    @Order(8)
    public void testCompleteWorkflow() throws InterruptedException {
        // Test complet du workflow
        System.out.println("=== Début du test complet ===");
        
        // 1. Login
        driver.get(baseUrl + "/login");
        Thread.sleep(1000);
        driver.findElement(By.id("username")).sendKeys("admin");
        Thread.sleep(500);
        driver.findElement(By.id("password")).sendKeys("admin");
        Thread.sleep(500);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        Thread.sleep(1000);
        System.out.println("✓ Login réussi");

        // 2. Voir la liste
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        Thread.sleep(1000);
        System.out.println("✓ Liste des produits affichée");

        // 3. Créer un nouveau produit
        driver.findElement(By.id("add-product-btn")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("name")).sendKeys("Final Test Product");
        Thread.sleep(500);
        driver.findElement(By.id("description")).sendKeys("Description du test final");
        Thread.sleep(500);
        driver.findElement(By.id("price")).sendKeys("149.99");
        Thread.sleep(500);
        driver.findElement(By.id("quantity")).sendKeys("25");
        Thread.sleep(1000);
        driver.findElement(By.id("save-product-btn")).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        Thread.sleep(1000);
        System.out.println("✓ Nouveau produit créé");

        // 4. Vérifier que le produit existe
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Final Test Product"));
        Thread.sleep(1000);
        System.out.println("✓ Produit visible dans la liste");

        // 5. Logout
        driver.findElement(By.cssSelector("button.btn-secondary")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Thread.sleep(1000);
        System.out.println("✓ Logout réussi");
        
        System.out.println("=== Test complet terminé avec succès ===");
    }

    private void login() {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/products"));
    }
}
