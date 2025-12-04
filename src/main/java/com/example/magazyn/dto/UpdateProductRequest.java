package com.example.magazyn.dto;

import com.example.magazyn.model.Dimensions;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateProductRequest {
    private String name;
    private Dimensions dimensions;
    private Set<Long> categoryIds;
}