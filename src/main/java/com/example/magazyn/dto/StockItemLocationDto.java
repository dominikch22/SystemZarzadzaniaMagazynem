package com.example.magazyn.dto;

import com.example.magazyn.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StockItemLocationDto {
    private Product product;
    private List<String> locationNames;
    private int quantity;
}