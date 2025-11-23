package com.example.magazyn.dto;

import com.example.magazyn.model.LocationType;
import lombok.Data;

@Data
public class CreateLocationRequest {
    private String name;
    private LocationType locationType;
    private Long parentId;
}
