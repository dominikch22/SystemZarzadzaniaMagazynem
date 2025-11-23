package com.example.magazyn.controller;

import com.example.magazyn.dto.CreateCompanyRequest;
import com.example.magazyn.dto.CreateLocationRequest;
import com.example.magazyn.dto.RegisterRequest;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.Location;
import com.example.magazyn.entity.User;
import com.example.magazyn.model.LocationType;
import com.example.magazyn.repository.CompanyRepository;
import com.example.magazyn.repository.LocationRepository;
import com.example.magazyn.repository.UserRepository;
import com.example.magazyn.service.AuthenticationService;
import com.example.magazyn.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocationControllerTest {

    @Autowired
    private LocationController locationController;

    @Autowired
    private LocationRepository locationRepository;

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
        locationRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        CreateCompanyRequest companyRequest = new CreateCompanyRequest();
        companyRequest.setName("Testowa Firma");
        testCompany = companyService.createCompany(companyRequest);

        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setLastname("lastname");
        userRequest.setFirstname("firstname");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("password");
        testUser = authenticationService.createUser(userRequest);
        testUser.setCompany(testCompany);
        userRepository.save(testUser);
    }

    @Test
    void shouldCreateWarehouseSuccessfully() {
        CreateLocationRequest request = new CreateLocationRequest();
        request.setName("Magazyn Centralny");
        request.setLocationType(LocationType.WAREHOUSE);

        ResponseEntity<Location> response = locationController.createLocation(request, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Location createdLocation = response.getBody();
        assertNotNull(createdLocation);
        assertNotNull(createdLocation.getId());
        assertEquals("Magazyn Centralny", createdLocation.getName());
        assertEquals(LocationType.WAREHOUSE, createdLocation.getLocationType());
        assertThat(createdLocation.getParentLocation()).isNull();

        assertThat(locationRepository.findAll()).hasSize(1)
                .first().extracting(Location::getName).isEqualTo("Magazyn Centralny");
    }

    @Test
    void shouldCreateRackSuccessfully() {
        Location warehouse = new Location();
        warehouse.setName("Magazyn Centralny");
        warehouse.setLocationType(LocationType.WAREHOUSE);
        warehouse.setCompany(testCompany);
        locationRepository.save(warehouse);

        CreateLocationRequest rackRequest = new CreateLocationRequest();
        rackRequest.setName("Regał A1");
        rackRequest.setLocationType(LocationType.RACK);
        rackRequest.setParentId(warehouse.getId());

        ResponseEntity<Location> response = locationController.createLocation(rackRequest, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Location createdRack = response.getBody();
        assertNotNull(createdRack);
        assertEquals("Regał A1", createdRack.getName());
        assertEquals(LocationType.RACK, createdRack.getLocationType());
        assertNotNull(createdRack.getParentLocation());
        assertEquals(warehouse.getId(), createdRack.getParentLocation().getId());

        Location savedRack = locationRepository.findByName("Regał A1").orElseThrow();
        assertThat(savedRack.getParentLocation()).isNotNull();
        assertThat(savedRack.getParentLocation().getId()).isEqualTo(warehouse.getId());
    }

    @Test
    void shouldCreateShelfSuccessfully() {
        Location warehouse = new Location();
        warehouse.setName("Magazyn Centralny");
        warehouse.setLocationType(LocationType.WAREHOUSE);
        warehouse.setCompany(testCompany);
        locationRepository.save(warehouse);

        Location rack = new Location();
        rack.setName("Regał A1");
        rack.setLocationType(LocationType.RACK);
        rack.setCompany(testCompany);
        rack.setParentLocation(warehouse);
        locationRepository.save(rack);

        CreateLocationRequest shelfRequest = new CreateLocationRequest();
        shelfRequest.setName("Półka 3");
        shelfRequest.setLocationType(LocationType.SHELF);
        shelfRequest.setParentId(rack.getId());

        ResponseEntity<Location> response = locationController.createLocation(shelfRequest, testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Location createdShelf = response.getBody();
        assertNotNull(createdShelf);
        assertEquals("Półka 3", createdShelf.getName());
        assertEquals(LocationType.SHELF, createdShelf.getLocationType());
        assertNotNull(createdShelf.getParentLocation());
        assertEquals(rack.getId(), createdShelf.getParentLocation().getId());

        Location savedShelf = locationRepository.findByName("Półka 3").orElseThrow();
        assertThat(savedShelf.getParentLocation()).isNotNull();
        assertThat(savedShelf.getParentLocation().getId()).isEqualTo(rack.getId());
    }
}

