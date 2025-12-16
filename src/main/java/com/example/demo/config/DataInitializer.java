package com.example.demo.config;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default user
        User user = new User();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        // Create sample products
        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("High-performance laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setQuantity(10);
        productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("Mouse");
        product2.setDescription("Wireless mouse");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setQuantity(50);
        productRepository.save(product2);

        Product product3 = new Product();
        product3.setName("Keyboard");
        product3.setDescription("Mechanical keyboard");
        product3.setPrice(new BigDecimal("79.99"));
        product3.setQuantity(30);
        productRepository.save(product3);
    }
}
