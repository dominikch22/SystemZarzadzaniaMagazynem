package com.example.magazyn.controller;

import com.example.magazyn.dto.CreateCategoryRequest;
import com.example.magazyn.dto.CreateCompanyRequest;
import com.example.magazyn.dto.RegisterRequest;
import com.example.magazyn.entity.Category;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.User;
import com.example.magazyn.repository.CategoryRepository;
import com.example.magazyn.repository.CompanyRepository;
import com.example.magazyn.repository.UserRepository;
import com.example.magazyn.service.AuthenticationService;
import com.example.magazyn.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CategoryControllerTest {

    @Autowired
    private CategoryController categoryController;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private AuthenticationService authenticationService;

    private User testUser;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        CreateCompanyRequest companyRequest = new CreateCompanyRequest();
        companyRequest.setName("Testowa Firma Kategoria");
        testCompany = companyService.createCompany(companyRequest);

        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setLastname("Kowalski");
        userRequest.setFirstname("Jan");
        userRequest.setEmail("jan.kowalski@test.com");
        userRequest.setPassword("password");
        testUser = authenticationService.createUser(userRequest);
        testUser.setCompany(testCompany);
        userRepository.save(testUser);
    }

    @Test
    void shouldCreateCategorySuccessfully() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Elektronika");

        ResponseEntity<Category> response = categoryController.createCategory(request, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Category createdCategory = response.getBody();
        assertNotNull(createdCategory);
        assertNotNull(createdCategory.getId());
        assertEquals("Elektronika", createdCategory.getName());
        assertEquals(testCompany.getId(), createdCategory.getCompany().getId());

        assertThat(categoryRepository.findAll()).hasSize(1)
                .first().extracting(Category::getName).isEqualTo("Elektronika");
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameAlreadyExistsInSameCompany() {
        CreateCategoryRequest request1 = new CreateCategoryRequest();
        request1.setName("Narzędzia");

        categoryController.createCategory(request1, testUser);

        CreateCategoryRequest request2 = new CreateCategoryRequest();
        request2.setName("Narzędzia");

        assertThrows(IllegalArgumentException.class, () -> {
            categoryController.createCategory(request2, testUser);
        });

        assertThat(categoryRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoCompany() {
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setLastname("Nowak");
        userRequest.setFirstname("Anna");
        userRequest.setEmail("anna.nowak@test.com");
        userRequest.setPassword("password");
        User userWithoutCompany = authenticationService.createUser(userRequest);

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Testowa Kategoria");

        assertThrows(IllegalStateException.class, () -> {
            categoryController.createCategory(request, userWithoutCompany);
        });

        assertThat(categoryRepository.findAll()).isEmpty();
    }
}