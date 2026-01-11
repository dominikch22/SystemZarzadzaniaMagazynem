package com.example.magazyn.dto;


import com.example.magazyn.entity.Product;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BestSellingItemDto {
    private Product product;
    private Long quantitySold;
}