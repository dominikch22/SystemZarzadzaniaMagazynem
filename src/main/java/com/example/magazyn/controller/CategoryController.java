package com.example.magazyn.controller;

import com.example.magazyn.dto.CreateCategoryRequest;
import com.example.magazyn.entity.Category;
import com.example.magazyn.entity.User;
import com.example.magazyn.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody CreateCategoryRequest createCategoryRequest,
            @AuthenticationPrincipal User user) {
        Category newCategory = categoryService.createCategory(createCategoryRequest, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }
}
