package com.example.magazyn.service;

import com.example.magazyn.dto.CreateStockMovementRequest;
import com.example.magazyn.entity.*;
import com.example.magazyn.repository.LocationRepository;
import com.example.magazyn.repository.ProductRepository;
import com.example.magazyn.repository.StockItemRepository;
import com.example.magazyn.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public StockMovementService(StockMovementRepository stockMovementRepository,
                                StockItemRepository stockItemRepository,
                                ProductRepository productRepository,
                                LocationRepository locationRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.stockItemRepository = stockItemRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public StockMovement createStockMovement(CreateStockMovementRequest request, User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Produkt nie znaleziony"));

        if (!product.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Brak dostępu do tego produktu.");
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Lokalizacja nie znaleziona"));

        if (!location.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Brak dostępu do tej lokalizacji.");
        }

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Ilość musi być większa od zera.");
        }

        updateStockAndProduct(request, product, location, company);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setQuantityChange(request.getQuantity());
        movement.setType(request.getType());
        return stockMovementRepository.save(movement);
    }

    private void updateStockAndProduct(CreateStockMovementRequest request, Product product, Location location, Company company) {
        StockItem stockItem = stockItemRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .orElse(null);

        if (stockItem == null) {
            if (request.getType() == StockMovement.MovementType.OUTBOUND) {
                throw new IllegalStateException("Nie można wydać towaru. Brak towaru w tej lokalizacji.");
            }
            stockItem = new StockItem();
            stockItem.setProduct(product);
            stockItem.setLocation(location);
            stockItem.setCompany(company);
            stockItem.setQuantity(0);
        }

        if (request.getType() == StockMovement.MovementType.INBOUND) {
            stockItem.setQuantity(stockItem.getQuantity() + request.getQuantity());
        } else {
            if (stockItem.getQuantity() < request.getQuantity()) {
                throw new IllegalStateException("Niewystarczająca ilość towaru w lokalizacji: " + location.getName());
            }
            stockItem.setQuantity(stockItem.getQuantity() - request.getQuantity());
        }

        stockItemRepository.save(stockItem);
        productRepository.save(product);
    }
}