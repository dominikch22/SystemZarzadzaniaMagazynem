package com.example.magazyn.repository;

import com.example.magazyn.entity.StockMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    @Query("SELECT sm.product, SUM(sm.quantityChange) as totalSold " +
            "FROM StockMovement sm " +
            "WHERE sm.type = 'OUTBOUND' " +
            "AND sm.movementDate >= :startDate " +
            "GROUP BY sm.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}