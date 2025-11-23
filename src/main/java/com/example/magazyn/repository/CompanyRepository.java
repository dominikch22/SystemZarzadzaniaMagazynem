package com.example.magazyn.repository;

import com.example.magazyn.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByName(String name);
    Optional<Company> findByName(String name);
}
