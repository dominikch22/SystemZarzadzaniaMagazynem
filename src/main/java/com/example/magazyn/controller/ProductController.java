package com.example.magazyn.controller;

import com.example.magazyn.dto.CreateProductRequest;
import com.example.magazyn.dto.StockItemLocationDto;
import com.example.magazyn.dto.UpdateProductRequest;
import com.example.magazyn.entity.Product;
import com.example.magazyn.entity.User;
import com.example.magazyn.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @RequestBody UpdateProductRequest request,
                                                 @AuthenticationPrincipal User user) {
        Product updatedProduct = productService.updateProduct(id, request, user);
        return ResponseEntity.ok(updatedProduct);
    }


    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.getAllProducts(user));
    }

    @GetMapping("/with-stock")
    public ResponseEntity<List<StockItemLocationDto>> getAllProductsWithQuantityAndLocations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(productService.getAllProductsWithStockDetails(user));
    }


}