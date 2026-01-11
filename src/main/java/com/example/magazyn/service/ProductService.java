package com.example.magazyn.service;

import com.example.magazyn.dto.*;
import com.example.magazyn.entity.Category;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.Product;
import com.example.magazyn.entity.User;
import com.example.magazyn.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;


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

    @Transactional
    public Product updateProduct(Long productId, UpdateProductRequest request, User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produkt o ID " + productId + " nie został znaleziony."));

        if (!product.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Brak uprawnień do edycji tego produktu.");
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }

        if (request.getCategoryIds() != null) {
            product.getCategories().clear();
            if (!request.getCategoryIds().isEmpty()) {
                Set<Category> newCategories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
                if (newCategories.size() != request.getCategoryIds().size()) {
                    throw new EntityNotFoundException("Nie znaleziono jednej lub więcej kategorii do przypisania.");
                }
                for (Category category : newCategories) {
                    if (!category.getCompany().getId().equals(company.getId())) {
                        throw new SecurityException("Brak uprawnień do przypisania produktu do kategorii innej firmy.");
                    }
                }
                product.setCategories(newCategories);
            }
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

    @Transactional()
    public List<StockItemLocationDto> getAllProductsWithStockDetails(User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }

        List<Product> products = productRepository.findAllByCompany(company);

        return products.stream()
                .map(this::mapToStockItemLocationDto)
                .collect(Collectors.toList());
    }

    private StockItemLocationDto mapToStockItemLocationDto(Product product) {
        Integer totalQuantity = stockItemRepository.sumQuantityByProductId(product.getId());

        List<String> locationNames = stockItemRepository.findLocationNamesByProductId(product.getId());

        return StockItemLocationDto.builder()
                .product(product)
                .locationNames(locationNames)
                .quantity(totalQuantity != null ? totalQuantity : 0)
                .build();
    }

    @Transactional()
    public BestSellingProductsReportDto getTopSellingProductsLastMonth(User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        List<Object[]> results = stockMovementRepository.findTopSellingProducts(
                oneMonthAgo,
                PageRequest.of(0, 20)
        );

        List<BestSellingItemDto> items = results.stream()
                .map(row -> {
                    Product product = (Product) row[0];
                    Long count = (Long) row[1];

                    if (!product.getCompany().getId().equals(company.getId())) {
                        return null;
                    }

                    return BestSellingItemDto.builder()
                            .product(product)
                            .quantitySold(count)
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return BestSellingProductsReportDto.builder()
                .bestSellingProducts(items)
                .build();
    }
}
