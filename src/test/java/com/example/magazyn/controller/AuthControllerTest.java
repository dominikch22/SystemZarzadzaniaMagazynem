package com.example.magazyn.controller;

import com.example.magazyn.dto.AuthRequest;
import com.example.magazyn.dto.RegisterRequest;
import com.example.magazyn.entity.Role;
import com.example.magazyn.entity.User;
import com.example.magazyn.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldCreateUserAndReturnToken() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstname("Test")
                .lastname("Test")
                .email("test.user@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        User savedUser = userRepository.findByEmail("test.user@example.com").orElseThrow();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFirstname()).isEqualTo("Test");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void authenticate_shouldReturnToken_whenCredentialsAreCorrect() throws Exception {
        User existingUser = User.builder()
                .firstname("Existing")
                .lastname("User")
                .email("existing.user@example.com")
                .password(passwordEncoder.encode("correct-password"))
                .role(Role.USER)
                .build();
        userRepository.save(existingUser);

        AuthRequest authRequest = AuthRequest.builder()
                .email("existing.user@example.com")
                .password("correct-password")
                .build();

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void authenticate_shouldReturnForbidden_whenPasswordIsIncorrect() throws Exception {
        User existingUser = User.builder()
                .firstname("Existing")
                .lastname("User")
                .email("another.user@example.com")
                .password(passwordEncoder.encode("correct-password"))
                .role(Role.USER)
                .build();
        userRepository.save(existingUser);

        AuthRequest authRequest = AuthRequest.builder()
                .email("another.user@example.com")
                .password("WRONG-password")
                .build();

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden());
    }
}