package com.example.magazyn.controller;

import com.example.magazyn.entity.Alert;
import com.example.magazyn.entity.User;
import com.example.magazyn.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<Alert>> getUnreadAlerts(@AuthenticationPrincipal User user) {
        if (user.getCompany() == null) {
            return ResponseEntity.ok(List.of());
        }
        List<Alert> unreadAlerts = alertService.getUnreadAlertsForCompany(user.getCompany());
        return ResponseEntity.ok(unreadAlerts);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAlertAsRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user.getCompany() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            alertService.markAlertAsRead(id, user.getCompany());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> forceGenerateAlerts(@AuthenticationPrincipal User user) {
        alertService.checkAndGenerateAllAlerts();
        return ResponseEntity.accepted().build();
    }
}