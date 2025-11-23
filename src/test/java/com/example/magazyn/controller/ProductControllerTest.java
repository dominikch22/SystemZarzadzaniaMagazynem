package com.example.magazyn.controller;

import com.example.magazyn.dto.*;
import com.example.magazyn.entity.Category;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.Product;
import com.example.magazyn.entity.User;
import com.example.magazyn.model.Dimensions;
import com.example.magazyn.repository.CategoryRepository;
import com.example.magazyn.repository.CompanyRepository;
import com.example.magazyn.repository.ProductRepository;
import com.example.magazyn.repository.UserRepository;
import com.example.magazyn.service.AuthenticationService;
import com.example.magazyn.service.CategoryService;
import com.example.magazyn.service.CompanyService;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProductControllerTest {

    @Autowired
    private ProductController productController;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CategoryService categoryService;

    private User testUser;
    private Company testCompany;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        CreateCompanyRequest companyRequest = new CreateCompanyRequest();
        companyRequest.setName("Testowa Firma Produkt");
        testCompany = companyService.createCompany(companyRequest);

        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setLastname("Smith");
        userRequest.setFirstname("John");
        userRequest.setEmail("john.smith@test.com");
        userRequest.setPassword("password");
        testUser = authenticationService.createUser(userRequest, testCompany);

        CreateCategoryRequest categoryRequest = new CreateCategoryRequest();
        categoryRequest.setName("Narzędzia");
        testCategory = categoryService.createCategory(categoryRequest, testUser);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Młotek");
        request.setDimensions(new Dimensions(10.0f, 3.0f, 30.0f));
        request.setCategoryIds(Collections.singleton(testCategory.getId()));

        ResponseEntity<Product> response = productController.createProduct(request, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Product createdProduct = response.getBody();
        assertNotNull(createdProduct);
        assertNotNull(createdProduct.getId());
        assertEquals("Młotek", createdProduct.getName());
        assertEquals(testCompany.getId(), createdProduct.getCompany().getId());
        assertEquals(1, createdProduct.getCategories().size());
        assertThat(createdProduct.getCategories()).extracting("id").contains(testCategory.getId());
        assertNotNull(createdProduct.getDimensions());
        assertEquals(10.0f, createdProduct.getDimensions().getX());

        assertThat(productRepository.findAll()).hasSize(1)
                .first().extracting(Product::getName).isEqualTo("Młotek");
    }

    @Test
    void shouldCreateProductWithoutCategoriesSuccessfully() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Śrubokręt");
        request.setDimensions(new Dimensions(2.0f, 2.0f, 15.0f));
        request.setCategoryIds(null);

        ResponseEntity<Product> response = productController.createProduct(request, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Product createdProduct = response.getBody();
        assertNotNull(createdProduct);
        assertEquals("Śrubokręt", createdProduct.getName());
        assertThat(createdProduct.getCategories()).isEmpty();

        assertThat(productRepository.findAll()).hasSize(1)
                .first().extracting(Product::getName).isEqualTo("Śrubokręt");
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoCompany() {
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setLastname("Nowak");
        userRequest.setFirstname("Anna");
        userRequest.setEmail("anna.nowak@test.com");
        userRequest.setPassword("password");
        User userWithoutCompany = authenticationService.createUser(userRequest);

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Produkt bez firmy");
        request.setDimensions(new Dimensions(1.0f, 1.0f, 1.0f));

        assertThrows(IllegalStateException.class, () -> {
            productController.createProduct(request, userWithoutCompany);
        });

        assertThat(productRepository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenCategoriesNotFound() {
        Set<Long> nonExistentCategoryIds = new HashSet<>();
        nonExistentCategoryIds.add(999L);
        nonExistentCategoryIds.add(testCategory.getId());

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Produkt z błędną kategorią");
        request.setDimensions(new Dimensions(5.0f, 5.0f, 5.0f));
        request.setCategoryIds(nonExistentCategoryIds);

        assertThrows(EntityNotFoundException.class, () -> {
            productController.createProduct(request, testUser);
        });

        assertThat(productRepository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowSecurityExceptionWhenCategoryFromDifferentCompany() {
        CreateCompanyRequest otherCompanyRequest = new CreateCompanyRequest();
        otherCompanyRequest.setName("Inna Firma");
        Company otherCompany = companyService.createCompany(otherCompanyRequest);

        RegisterRequest otherUserRequest = new RegisterRequest();
        otherUserRequest.setLastname("Other");
        otherUserRequest.setFirstname("User");
        otherUserRequest.setEmail("other.user@test.com");
        otherUserRequest.setPassword("password");
        User otherUser = authenticationService.createUser(otherUserRequest, otherCompany);

        CreateCategoryRequest otherCategoryRequest = new CreateCategoryRequest();
        otherCategoryRequest.setName("Kategoria Innej Firmy");
        Category otherCategory = categoryService.createCategory(otherCategoryRequest, otherUser);

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Produkt z kategorią innej firmy");
        request.setDimensions(new Dimensions(1.0f, 1.0f, 1.0f));
        request.setCategoryIds(Collections.singleton(otherCategory.getId()));

        assertThrows(SecurityException.class, () -> {
            productController.createProduct(request, testUser);
        });

        assertThat(productRepository.findAll()).isEmpty();
    }
}
