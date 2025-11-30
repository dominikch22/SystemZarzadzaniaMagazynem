package com.example.magazyn.dto;

import lombok.Data;

@Data
public class AssignUserToCompanyRequest {
    private String email;
    private Long companyId;
}