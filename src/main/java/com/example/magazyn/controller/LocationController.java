package com.example.magazyn.controller;

import com.example.magazyn.dto.CreateLocationRequest;
import com.example.magazyn.entity.Location;
import com.example.magazyn.entity.User;
import com.example.magazyn.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(
            @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal User user) {
        Location createdLocation = locationService.createLocation(request, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }
}