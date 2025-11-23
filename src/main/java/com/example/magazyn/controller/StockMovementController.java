package com.example.magazyn.controller;

import com.example.magazyn.dto.CreateStockMovementRequest;
import com.example.magazyn.entity.StockMovement;
import com.example.magazyn.entity.User;
import com.example.magazyn.service.StockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @Autowired
    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    public ResponseEntity<StockMovement> createMovement(
            @RequestBody CreateStockMovementRequest request,
            @AuthenticationPrincipal User user) {

        StockMovement movement = stockMovementService.createStockMovement(request, user);
        return ResponseEntity.ok(movement);
    }
}