package com.example.magazyn.repository;

import com.example.magazyn.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductIdAndLocationId(Long productId, Long locationId);
}
