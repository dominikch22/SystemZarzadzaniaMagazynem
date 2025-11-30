package com.example.magazyn.config;

import com.example.magazyn.entity.Role;
import com.example.magazyn.entity.User;
import com.example.magazyn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.admin.email}")
    private String adminEmail;

    @Value("${application.security.admin.password}")
    private String adminPassword;

    @Value("${application.security.admin.firstname}")
    private String adminFirstname;

    @Value("${application.security.admin.lastname}")
    private String adminLastname;

    @Override
    public void run(String... args) throws Exception {
        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);

        if (existingAdmin.isEmpty()) {
            User admin = User.builder()
                    .firstname(adminFirstname)
                    .lastname(adminLastname)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .company(null)
                    .build();

            userRepository.save(admin);
            log.info("Konto administratora zostało utworzone: {}", adminEmail);
        } else {
            log.info("Konto administratora już istnieje: {}", adminEmail);
        }
    }
}