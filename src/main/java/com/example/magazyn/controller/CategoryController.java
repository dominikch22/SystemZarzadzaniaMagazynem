package com.example.magazyn.controller;

import com.example.magazyn.dto.CategoryDto;
import com.example.magazyn.dto.CreateCategoryRequest;
import com.example.magazyn.entity.Category;
import com.example.magazyn.entity.User;
import com.example.magazyn.mapper.CategoryMapper;
import com.example.magazyn.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @RequestBody CreateCategoryRequest createCategoryRequest,
            @AuthenticationPrincipal User user) {
        Category newCategory = categoryService.createCategory(createCategoryRequest, user);

        CategoryDto categoryDto = categoryMapper.toDto(newCategory);

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryDto);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories(@AuthenticationPrincipal User user) {
        List<Category> categories = categoryService.getAllCategories(user);

        List<CategoryDto> categoryDtos = categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDtos);
    }
}
