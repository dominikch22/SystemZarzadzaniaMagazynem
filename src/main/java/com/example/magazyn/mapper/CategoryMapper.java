package com.example.magazyn.mapper;

import com.example.magazyn.dto.CategoryDto;
import com.example.magazyn.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .companyId(category.getCompany() != null ? category.getCompany().getId() : null)
                .build();
    }
}