package com.example.magazyn.service;

import com.example.magazyn.dto.CreateCompanyRequest;
import com.example.magazyn.entity.Company;
import com.example.magazyn.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Firma o nazwie '" + request.getName() + "' już istnieje.");
        }

        Company company = new Company();
        company.setName(request.getName());

        return companyRepository.save(company);
    }
}