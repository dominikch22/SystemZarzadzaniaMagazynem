package com.example.magazyn.controller;

import com.example.magazyn.dto.AssignUserToCompanyRequest;
import com.example.magazyn.dto.CreateCompanyRequest;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.User;
import com.example.magazyn.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Company> createCompany(@RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.createCompany(request));
    }

    @PutMapping("/users/assign-company")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> assignUserToCompany(@RequestBody AssignUserToCompanyRequest request) {
        User updatedUser = companyService.assignUserToCompany(request);
        return ResponseEntity.ok(updatedUser);
    }
}
