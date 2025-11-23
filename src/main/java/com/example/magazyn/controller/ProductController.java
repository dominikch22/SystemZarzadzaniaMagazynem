package com.example.magazyn.controller;

import com.example.magazyn.dto.CreateProductRequest;
import com.example.magazyn.entity.Product;
import com.example.magazyn.entity.User;
import com.example.magazyn.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request,
                                                 @AuthenticationPrincipal User user) {
        Product createdProduct = productService.createProduct(request, user);

        return ResponseEntity.ok().body(createdProduct);
    }
}