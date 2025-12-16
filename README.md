# Selenium Spring Boot Project

## Lancer l'application

```bash
mvn spring-boot:run
```

Accéder à l'application: http://localhost:8080/login
- Username: admin
- Password: admin

## Lancer les tests Selenium (avec navigateur visible)

```bash
mvn test
```

Les tests s'exécutent avec Chrome en mode visible pour observer les actions.

## Tests disponibles

1. testLoginPage - Vérifie la page de login
2. testSuccessfulLogin - Test de connexion
3. testProductListPage - Vérifie la liste des produits
4. testCreateNewProduct - Création d'un produit
5. testEditProduct - Modification d'un produit
6. testDeleteProduct - Suppression d'un produit
7. testLogout - Test de déconnexion
8. testCompleteWorkflow - Test complet du workflow

## Structure

- Login avec Spring Security
- CRUD complet sur les produits
- Interface Thymeleaf responsive
- Tests end-to-end avec Selenium
