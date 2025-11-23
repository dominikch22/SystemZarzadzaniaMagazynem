package com.example.magazyn.service;

import com.example.magazyn.dto.CreateProductRequest;
import com.example.magazyn.entity.*;
import com.example.magazyn.repository.CategoryRepository;
import com.example.magazyn.repository.LocationRepository;
import com.example.magazyn.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, LocationRepository locationRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public Product createProduct(CreateProductRequest request, User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDimensions(request.getDimensions());
        product.setCompany(company);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            if (categories.size() != request.getCategoryIds().size()) {
                throw new EntityNotFoundException("Nie znaleziono jednej lub więcej kategorii.");
            }
            for (Category category : categories) {
                if (!category.getCompany().getId().equals(company.getId())) {
                    throw new SecurityException("Brak uprawnień do przypisania produktu do kategorii innej firmy.");
                }
            }
            product.setCategories(categories);
        }

        return productRepository.save(product);
    }

    public List<Product> getAllProducts(User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }
        return productRepository.findAllByCompany(company);
    }
}
