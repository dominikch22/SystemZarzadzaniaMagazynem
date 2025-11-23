package com.example.magazyn.dto;

import com.example.magazyn.entity.StockMovement;
import lombok.Data;

@Data
public class CreateStockMovementRequest {
    private Long productId;
    private Long locationId;
    private int quantity;
    private StockMovement.MovementType type;
}
