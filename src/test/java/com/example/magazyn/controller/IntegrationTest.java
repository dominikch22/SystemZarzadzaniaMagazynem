package com.example.magazyn.controller;

import com.example.magazyn.dto.*;
import com.example.magazyn.entity.Category;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.Location;
import com.example.magazyn.entity.Product;
import com.example.magazyn.entity.StockMovement;
import com.example.magazyn.model.Dimensions;
import com.example.magazyn.model.LocationType;
import com.example.magazyn.repository.CompanyRepository;
import com.example.magazyn.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String AUTHENTICATE_URL = "/api/auth/authenticate";
    private static final String CATEGORY_URL = "/api/categories";
    private static final String PRODUCT_URL = "/api/products";
    private static final String LOCATION_URL = "/api/locations"; // Dodano
    private static final String MOVEMENT_URL = "/api/movements"; // Dodano

    private String registeredUserEmail = "integration.test@example.com";
    private String registeredUserPassword = "securePassword";

    private String jwtToken;
    private Long companyId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        companyRepository.deleteAll();

        // 1. Rejestracja
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstname("Test")
                .lastname("Integration")
                .email(registeredUserEmail)
                .password(registeredUserPassword)
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Uwierzytelnienie i pobranie tokena
        AuthRequest authRequest = AuthRequest.builder()
                .email(registeredUserEmail)
                .password(registeredUserPassword)
                .build();

        MvcResult result = mockMvc.perform(post(AUTHENTICATE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        jwtToken = authResponse.getToken();

        Company company = companyRepository.findByName("TEST_COMPANY").orElseThrow();
        companyId = company.getId();
    }

    @Test
    void shouldCreateProductAndStockMovementAndFetchStockDetails() throws Exception {

        CreateLocationRequest locationRequest = new CreateLocationRequest();
        locationRequest.setName("Integration Warehouse");
        locationRequest.setLocationType(LocationType.WAREHOUSE);

        MvcResult locationResult = mockMvc.perform(post(LOCATION_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Location createdLocation = objectMapper.readValue(locationResult.getResponse().getContentAsString(), Location.class);
        Long locationId = createdLocation.getId();

        CreateCategoryRequest categoryRequest = new CreateCategoryRequest();
        categoryRequest.setName("Test Category Stock");

        MvcResult categoryResult = mockMvc.perform(post(CATEGORY_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper.readValue(categoryResult.getResponse().getContentAsString(), Category.class).getId();


        CreateProductRequest productRequest = new CreateProductRequest();
        productRequest.setName("Tested Item");
        productRequest.setDimensions(new Dimensions(1f, 1f, 1f));
        productRequest.setCategoryIds(Set.of(categoryId));

        MvcResult productResult = mockMvc.perform(post(PRODUCT_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Product createdProduct = objectMapper.readValue(productResult.getResponse().getContentAsString(), Product.class);
        Long productId = createdProduct.getId();

        assertThat(productId).isNotNull();



        int quantity = 42;
        CreateStockMovementRequest movementRequest = new CreateStockMovementRequest();
        movementRequest.setProductId(productId);
        movementRequest.setLocationId(locationId);
        movementRequest.setQuantity(quantity);
        movementRequest.setType(StockMovement.MovementType.INBOUND);

        String test = objectMapper.writeValueAsString(movementRequest);
        mockMvc.perform(post(MOVEMENT_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movementRequest)))
                .andExpect(status().isOk());



        MvcResult fetchResult = mockMvc.perform(get(PRODUCT_URL + "/with-stock")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String fetchResponseBody = fetchResult.getResponse().getContentAsString();
        System.out.println("Response /with-stock: " + fetchResponseBody); // Debug

        List<StockItemLocationDto> fetchedStockDetails = objectMapper.readValue(
                fetchResponseBody,
                new TypeReference<List<StockItemLocationDto>>() {}
        );

        assertThat(fetchedStockDetails).hasSize(1);
        StockItemLocationDto stockItem = fetchedStockDetails.get(0);

        assertThat(stockItem.getQuantity()).isEqualTo(quantity);

        assertThat(stockItem.getLocationNames()).containsExactlyInAnyOrder("Integration Warehouse");

        assertThat(stockItem.getProduct().getName()).isEqualTo("Tested Item");
    }

    @Test
    void shouldRegisterCreateCategoryAndProductAndFetchListSimple() throws Exception {
    }
}