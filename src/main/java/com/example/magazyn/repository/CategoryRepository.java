package com.example.magazyn.repository;

import com.example.magazyn.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndCompanyId(String name, Long companyId);
}
