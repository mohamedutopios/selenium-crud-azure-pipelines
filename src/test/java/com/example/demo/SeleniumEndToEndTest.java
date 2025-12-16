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
        
        // ========================================
        // DÉTECTION AUTOMATIQUE DE L'ENVIRONNEMENT
        // ========================================
        boolean isCI = System.getenv("CI") != null || 
                       System.getenv("AGENT_NAME") != null ||  // Azure DevOps
                       System.getenv("JENKINS_HOME") != null || // Jenkins
                       System.getenv("GITHUB_ACTIONS") != null; // GitHub Actions
        
        if (isCI) {
            // ====================================
            // MODE CI/CD - HEADLESS
            // ====================================
            System.out.println("🤖 Mode CI/CD détecté - Configuration headless");
            options.addArguments("--headless=new");          // Nouveau mode headless (Chrome 109+)
            options.addArguments("--no-sandbox");             // Évite problèmes de permissions
            options.addArguments("--disable-dev-shm-usage");  // Évite problèmes mémoire partagée
            options.addArguments("--disable-gpu");            // Désactive GPU
            options.addArguments("--window-size=1920,1080");  // Taille fixe
            options.addArguments("--disable-extensions");
            options.addArguments("--proxy-server='direct://'");
            options.addArguments("--proxy-bypass-list=*");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-background-networking");
            options.addArguments("--disable-default-apps");
            options.addArguments("--disable-sync");
            options.addArguments("--metrics-recording-only");
            options.addArguments("--mute-audio");
            options.addArguments("--no-first-run");
            options.addArguments("--safebrowsing-disable-auto-update");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--remote-debugging-port=9222"); // Pour debug si besoin
        } else {
            // ====================================
            // MODE LOCAL - NAVIGATEUR VISIBLE
            // ====================================
            System.out.println("🖥️  Mode local détecté - Navigateur visible");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
        }
        
        // Options communes
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        
        try {
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            baseUrl = "http://localhost:" + port;
            
            System.out.println("✅ WebDriver initialisé - URL: " + baseUrl);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation du WebDriver: " + e.getMessage());
            throw e;
        }
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("✅ WebDriver fermé proprement");
            } catch (Exception e) {
                System.err.println("⚠️  Erreur lors de la fermeture: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    public void testLoginPage() {
        System.out.println("\n[TEST 1] 🧪 Test de la page de login");
        
        driver.get(baseUrl + "/login");
        
        // Attendre que la page soit chargée
        wait.until(ExpectedConditions.titleIs("Login"));
        
        String pageTitle = driver.getTitle();
        assertEquals("Login", pageTitle);

        WebElement usernameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        assertNotNull(usernameField);
        assertNotNull(passwordField);
        assertNotNull(loginButton);
        
        System.out.println("✅ Page de login OK");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        System.out.println("\n[TEST 2] 🔐 Test de login réussi");
        
        driver.get(baseUrl + "/login");

        WebElement usernameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        WebElement passwordField = driver.findElement(By.id("password"));

        usernameField.sendKeys("admin");
        passwordField.sendKeys("admin");

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Attendre la redirection vers /products
        wait.until(ExpectedConditions.urlContains("/products"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/products"));
        
        System.out.println("✅ Login réussi, redirection OK");
    }

    @Test
    @Order(3)
    public void testProductListPage() {
        System.out.println("\n[TEST 3] 📋 Test de la page liste des produits");
        
        login();

        // Vérifier le titre
        wait.until(ExpectedConditions.titleIs("Products List"));
        String pageTitle = driver.getTitle();
        assertEquals("Products List", pageTitle);

        // Vérifier la présence du tableau
        WebElement table = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("products-table"))
        );
        assertNotNull(table);

        // Vérifier qu'il y a des produits
        List<WebElement> rows = driver.findElements(
            By.cssSelector("#products-table tbody tr")
        );
        assertTrue(rows.size() > 0, "Il devrait y avoir des produits dans la liste");
        
        System.out.println("✅ Liste des produits OK - " + rows.size() + " produit(s)");
    }

    @Test
    @Order(4)
    public void testCreateNewProduct() {
        System.out.println("\n[TEST 4] ➕ Test de création d'un produit");
        
        login();

        // Cliquer sur "Add New Product"
        WebElement addButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("add-product-btn"))
        );
        addButton.click();

        // Attendre d'être sur la page de création
        wait.until(ExpectedConditions.urlContains("/products/new"));

        // Remplir le formulaire
        WebElement nameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("name"))
        );
        WebElement descriptionField = driver.findElement(By.id("description"));
        WebElement priceField = driver.findElement(By.id("price"));
        WebElement quantityField = driver.findElement(By.id("quantity"));

        nameField.sendKeys("Test Product " + System.currentTimeMillis());
        descriptionField.sendKeys("This is a test product");
        priceField.sendKeys("99.99");
        quantityField.sendKeys("15");

        // Soumettre le formulaire
        WebElement saveButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("save-product-btn"))
        );
        saveButton.click();

        // Attendre la redirection vers la liste
        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));

        // Vérifier que le produit apparaît dans la liste
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Test Product"));
        
        System.out.println("✅ Produit créé avec succès");
    }

    @Test
    @Order(5)
    public void testEditProduct() {
        System.out.println("\n[TEST 5] ✏️  Test de modification d'un produit");
        
        login();

        // Trouver le premier bouton Edit
        WebElement editButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-warning"))
        );
        editButton.click();

        // Attendre d'être sur la page d'édition
        wait.until(ExpectedConditions.urlContains("/products/edit/"));

        // Modifier le nom
        WebElement nameField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("name"))
        );
        nameField.clear();
        nameField.sendKeys("Updated Product " + System.currentTimeMillis());

        // Soumettre
        WebElement updateButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-primary"))
        );
        updateButton.click();

        // Attendre la redirection
        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));

        // Vérifier que le produit a été modifié
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Updated Product"));
        
        System.out.println("✅ Produit modifié avec succès");
    }

    @Test
    @Order(6)
    public void testDeleteProduct() {
        System.out.println("\n[TEST 6] 🗑️  Test de suppression d'un produit");
        
        login();

        // Compter le nombre de produits avant suppression
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        List<WebElement> rowsBefore = driver.findElements(
            By.cssSelector("#products-table tbody tr")
        );
        int countBefore = rowsBefore.size();
        System.out.println("Nombre de produits avant suppression: " + countBefore);

        // Trouver et cliquer sur le premier bouton Delete
        WebElement deleteButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-danger"))
        );
        deleteButton.click();

        // Attendre et accepter l'alerte de confirmation
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        // Attendre le rechargement de la page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        
        // Petite pause pour que le DOM se mette à jour
        wait.until(driver2 -> {
            List<WebElement> rows = driver.findElements(
                By.cssSelector("#products-table tbody tr")
            );
            return rows.size() == countBefore - 1;
        });

        // Vérifier que le nombre de produits a diminué
        List<WebElement> rowsAfter = driver.findElements(
            By.cssSelector("#products-table tbody tr")
        );
        int countAfter = rowsAfter.size();
        
        assertEquals(countBefore - 1, countAfter, 
            "Le nombre de produits devrait avoir diminué de 1");
        
        System.out.println("✅ Produit supprimé - Nombre restant: " + countAfter);
    }

    @Test
    @Order(7)
    public void testLogout() {
        System.out.println("\n[TEST 7] 🚪 Test de déconnexion");
        
        login();

        // Cliquer sur Logout
        WebElement logoutButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-secondary"))
        );
        logoutButton.click();

        // Attendre d'être redirigé vers la page de login
        wait.until(ExpectedConditions.urlContains("/login"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"));
        
        System.out.println("✅ Déconnexion réussie");
    }

    @Test
    @Order(8)
    public void testCompleteWorkflow() {
        System.out.println("\n[TEST 8] 🎯 Test du workflow complet");
        System.out.println("========================================");
        
        // 1. Login
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        System.out.println("  ✓ Login réussi");

        // 2. Voir la liste
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        System.out.println("  ✓ Liste des produits affichée");

        // 3. Créer un nouveau produit
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-product-btn"))).click();
        wait.until(ExpectedConditions.urlContains("/products/new"));
        
        String productName = "Final Test Product " + System.currentTimeMillis();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")))
            .sendKeys(productName);
        driver.findElement(By.id("description")).sendKeys("Description du test final");
        driver.findElement(By.id("price")).sendKeys("149.99");
        driver.findElement(By.id("quantity")).sendKeys("25");
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("save-product-btn"))).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        System.out.println("  ✓ Nouveau produit créé: " + productName);

        // 4. Vérifier que le produit existe
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products-table")));
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Final Test Product"));
        System.out.println("  ✓ Produit visible dans la liste");

        // 5. Logout
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-secondary")
        )).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        System.out.println("  ✓ Logout réussi");
        
        System.out.println("========================================");
        System.out.println("✅ Test complet terminé avec succès");
    }

    // ========================================
    // MÉTHODE UTILITAIRE
    // ========================================
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
}