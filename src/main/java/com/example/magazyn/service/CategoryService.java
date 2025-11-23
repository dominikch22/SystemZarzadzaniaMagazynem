package com.example.magazyn.service;

import com.example.magazyn.dto.CreateCategoryRequest;
import com.example.magazyn.entity.Category;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.User;
import com.example.magazyn.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest request, User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }

        if (categoryRepository.existsByNameAndCompanyId(request.getName(), company.getId())) {
            throw new IllegalArgumentException("Kategoria o nazwie '" + request.getName() + "' już istnieje w tej firmie.");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setCompany(company);

        return categoryRepository.save(category);
    }
}
