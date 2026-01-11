package com.example.magazyn.controller;

import com.example.magazyn.dto.AuthRequest;
import com.example.magazyn.dto.AuthResponse;
import com.example.magazyn.dto.RegisterRequest;
import com.example.magazyn.entity.*;
import com.example.magazyn.model.Dimensions;
import com.example.magazyn.model.LocationType;
import com.example.magazyn.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private Company testCompany;

    @BeforeEach
    void setUp() throws Exception {
        alertRepository.deleteAll();
        stockItemRepository.deleteAll();
        productRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        testCompany = new Company();
        testCompany.setName("Alert Test Corp");
        testCompany = companyRepository.save(testCompany);

        RegisterRequest userReg = RegisterRequest.builder()
                .firstname("Jan")
                .lastname("Kowalski")
                .email("user@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReg)))
                .andExpect(status().isOk());

        User user = userRepository.findByEmail("user@test.com").orElseThrow();
        user.setCompany(testCompany);
        userRepository.save(user);

        userToken = getJwtToken("user@test.com", "password123");

        User admin = User.builder()
                .firstname("Admin")
                .lastname("Adminowski")
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .company(testCompany)
                .build();
        userRepository.save(admin);

        adminToken = getJwtToken("admin@test.com", "password123");
    }

    private String getJwtToken(String email, String password) throws Exception {
        AuthRequest authRequest = AuthRequest.builder().email(email).password(password).build();
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, AuthResponse.class).getToken();
    }

    @Test
    void shouldGetUnreadAlerts() throws Exception {
        Alert alert = new Alert();
        alert.setType(Alert.AlertType.LOW_STOCK);
        alert.setMessage("Testowy alert niskiego stanu");
        alert.setCompany(testCompany);
        alert.setRead(false);
        alert.setTimestamp(LocalDateTime.now());
        alertRepository.save(alert);

        mockMvc.perform(get("/api/alerts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").value("Testowy alert niskiego stanu"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void shouldMarkAlertAsRead() throws Exception {
        Alert alert = new Alert();
        alert.setType(Alert.AlertType.EXPIRY_WARNING);
        alert.setMessage("Wkrótce wygaśnie");
        alert.setCompany(testCompany);
        alert.setRead(false);
        alertRepository.save(alert);

        Long alertId = alert.getId();

        mockMvc.perform(patch("/api/alerts/" + alertId + "/read")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        Alert updatedAlert = alertRepository.findById(alertId).orElseThrow();
        assertThat(updatedAlert.isRead()).isTrue();
    }

    @Test
    void shouldGenerateAlertsWhenStockIsLow() throws Exception {
        Product product = new Product();
        product.setName("Produkt Krytyczny");
        product.setCompany(testCompany);
        product.setDimensions(new Dimensions(1, 1, 1));
        product = productRepository.save(product);

        Location loc = new Location();
        loc.setName("Magazyn A");
        loc.setLocationType(LocationType.WAREHOUSE);
        loc.setCompany(testCompany);
        loc = locationRepository.save(loc);

        StockItem stock = new StockItem();
        stock.setProduct(product);
        stock.setLocation(loc);
        stock.setCompany(testCompany);
        stock.setQuantity(5);
        stockItemRepository.save(stock);

        mockMvc.perform(post("/api/alerts/generate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isAccepted());

        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).isNotEmpty();
        assertThat(alerts.stream().anyMatch(a -> a.getMessage().contains("Produkt Krytyczny"))).isTrue();
    }

    @Test
    void shouldReturnForbiddenWhenUserTriesToGenerateAlerts() throws Exception {
        mockMvc.perform(post("/api/alerts/generate")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}