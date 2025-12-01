package com.example.magazyn.repository;

import com.example.magazyn.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductIdAndLocationId(Long productId, Long locationId);


    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM StockItem si WHERE si.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT l.name FROM StockItem si JOIN si.location l WHERE si.product.id = :productId AND si.quantity > 0")
    List<String> findLocationNamesByProductId(@Param("productId") Long productId);
}
