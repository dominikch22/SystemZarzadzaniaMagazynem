package com.example.magazyn.service;

import com.example.magazyn.dto.AssignUserToCompanyRequest;
import com.example.magazyn.dto.CreateCompanyRequest;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.User;
import com.example.magazyn.repository.CompanyRepository;
import com.example.magazyn.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;


    @Transactional
    public Company createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Firma o nazwie '" + request.getName() + "' już istnieje.");
        }

        Company company = new Company();
        company.setName(request.getName());

        return companyRepository.save(company);
    }

    @Transactional
    public User assignUserToCompany(AssignUserToCompanyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika o emailu: " + request.getEmail()));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Firma o ID " + request.getCompanyId() + " nie istnieje."));

        user.setCompany(company);

        return userRepository.save(user);
    }
}