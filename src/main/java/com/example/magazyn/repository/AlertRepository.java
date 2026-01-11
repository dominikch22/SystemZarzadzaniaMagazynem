package com.example.magazyn.repository;


import com.example.magazyn.entity.Alert;
import com.example.magazyn.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByCompanyAndIsReadOrderByTimestampDesc(Company company, boolean isRead);
}