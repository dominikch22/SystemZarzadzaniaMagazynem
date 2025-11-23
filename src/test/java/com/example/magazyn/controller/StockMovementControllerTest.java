package com.example.magazyn.controller;

import com.example.magazyn.dto.*;
import com.example.magazyn.entity.*;
import com.example.magazyn.model.Dimensions;
import com.example.magazyn.model.LocationType;
import com.example.magazyn.repository.*;
import com.example.magazyn.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StockMovementControllerTest {

    @Autowired
    private StockMovementController stockMovementController;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Product testProduct;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        stockMovementRepository.deleteAll();
        stockItemRepository.deleteAll();
        productRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        CreateCompanyRequest companyRequest = new CreateCompanyRequest();
        companyRequest.setName("Logistics Pro");
        Company testCompany = companyService.createCompany(companyRequest);

        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setFirstname("Adam");
        userRequest.setLastname("Manager");
        userRequest.setEmail("adam@logistics.pro");
        userRequest.setPassword("securePass123");
        testUser = authenticationService.createUser(userRequest, testCompany);

        CreateLocationRequest locationRequest = new CreateLocationRequest();
        locationRequest.setName("Magazyn Główny");
        locationRequest.setLocationType(LocationType.WAREHOUSE);
        testLocation = locationService.createLocation(locationRequest, testUser);

        CreateProductRequest productRequest = new CreateProductRequest();
        productRequest.setName("Laptop Dell");
        productRequest.setDimensions(new Dimensions(30f, 20f, 2f));
        productRequest.setCategoryIds(Collections.emptySet());
        testProduct = productService.createProduct(productRequest, testUser);
    }

    @Test
    void shouldProcessInboundMovementSuccessfully() {
        CreateStockMovementRequest request = new CreateStockMovementRequest();
        request.setProductId(testProduct.getId());
        request.setLocationId(testLocation.getId());
        request.setQuantity(10);
        request.setType(StockMovement.MovementType.INBOUND);

        ResponseEntity<StockMovement> response = stockMovementController.createMovement(request, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StockMovement movement = response.getBody();
        assertThat(movement).isNotNull();
        assertThat(movement.getQuantityChange()).isEqualTo(10);
        assertThat(movement.getType()).isEqualTo(StockMovement.MovementType.INBOUND);

        StockItem stockItem = stockItemRepository.findByProductIdAndLocationId(testProduct.getId(), testLocation.getId())
                .orElseThrow();
        assertThat(stockItem.getQuantity()).isEqualTo(10);

        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(10);

        assertThat(stockMovementRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldProcessOutboundMovementSuccessfully() {
        CreateStockMovementRequest inboundRequest = new CreateStockMovementRequest();
        inboundRequest.setProductId(testProduct.getId());
        inboundRequest.setLocationId(testLocation.getId());
        inboundRequest.setQuantity(50);
        inboundRequest.setType(StockMovement.MovementType.INBOUND);
        stockMovementController.createMovement(inboundRequest, testUser);

        CreateStockMovementRequest outboundRequest = new CreateStockMovementRequest();
        outboundRequest.setProductId(testProduct.getId());
        outboundRequest.setLocationId(testLocation.getId());
        outboundRequest.setQuantity(20);
        outboundRequest.setType(StockMovement.MovementType.OUTBOUND);

        ResponseEntity<StockMovement> response = stockMovementController.createMovement(outboundRequest, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getQuantityChange()).isEqualTo(20);
        assertThat(response.getBody().getType()).isEqualTo(StockMovement.MovementType.OUTBOUND);

        StockItem stockItem = stockItemRepository.findByProductIdAndLocationId(testProduct.getId(), testLocation.getId())
                .orElseThrow();
        assertThat(stockItem.getQuantity()).isEqualTo(30);

        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(30);
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStockForOutbound() {
        CreateStockMovementRequest inboundRequest = new CreateStockMovementRequest();
        inboundRequest.setProductId(testProduct.getId());
        inboundRequest.setLocationId(testLocation.getId());
        inboundRequest.setQuantity(5);
        inboundRequest.setType(StockMovement.MovementType.INBOUND);
        stockMovementController.createMovement(inboundRequest, testUser);

        CreateStockMovementRequest outboundRequest = new CreateStockMovementRequest();
        outboundRequest.setProductId(testProduct.getId());
        outboundRequest.setLocationId(testLocation.getId());
        outboundRequest.setQuantity(10);
        outboundRequest.setType(StockMovement.MovementType.OUTBOUND);

        assertThrows(IllegalStateException.class, () -> {
            stockMovementController.createMovement(outboundRequest, testUser);
        });

        StockItem stockItem = stockItemRepository.findByProductIdAndLocationId(testProduct.getId(), testLocation.getId()).orElseThrow();
        assertThat(stockItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldThrowExceptionWhenUserAccessesResourceFromDifferentCompany() {
        CreateCompanyRequest otherCompanyRequest = new CreateCompanyRequest();
        otherCompanyRequest.setName("Competitor Inc.");
        Company otherCompany = companyService.createCompany(otherCompanyRequest);

        RegisterRequest otherUserRequest = new RegisterRequest();
        otherUserRequest.setFirstname("Spy");
        otherUserRequest.setLastname("User");
        otherUserRequest.setEmail("spy@competitor.com");
        otherUserRequest.setPassword("password");
        User otherUser = authenticationService.createUser(otherUserRequest, otherCompany);

        CreateProductRequest otherProductRequest = new CreateProductRequest();
        otherProductRequest.setName("Alien Tech");
        otherProductRequest.setDimensions(new Dimensions(10f, 10f, 10f));
        Product otherProduct = productService.createProduct(otherProductRequest, otherUser);

        CreateStockMovementRequest request = new CreateStockMovementRequest();
        request.setProductId(otherProduct.getId());
        request.setLocationId(testLocation.getId());
        request.setQuantity(5);
        request.setType(StockMovement.MovementType.INBOUND);

        assertThrows(SecurityException.class, () -> {
            stockMovementController.createMovement(request, testUser);
        });

        assertThat(stockItemRepository.findByProductIdAndLocationId(otherProduct.getId(), testLocation.getId()))
                .isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        CreateStockMovementRequest request = new CreateStockMovementRequest();
        request.setProductId(9999L);
        request.setLocationId(testLocation.getId());
        request.setQuantity(5);
        request.setType(StockMovement.MovementType.INBOUND);

        assertThrows(EntityNotFoundException.class, () -> {
            stockMovementController.createMovement(request, testUser);
        });
    }
}