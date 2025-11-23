package com.example.magazyn.repository;

import com.example.magazyn.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByName(String name);
}
