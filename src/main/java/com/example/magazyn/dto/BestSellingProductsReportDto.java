package com.example.magazyn.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BestSellingProductsReportDto {
    private List<BestSellingItemDto> bestSellingProducts;
}
