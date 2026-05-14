package com.haryokuncoro.ops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haryokuncoro.ops.dto.LoginRequest;
import com.haryokuncoro.ops.dto.RegisterRequest;
import com.haryokuncoro.ops.entity.User;
import com.haryokuncoro.ops.security.JwtAuthenticationFilter;
import com.haryokuncoro.ops.service.AuthService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should register successfully")
    void shouldRegisterSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mail.com");
        request.setPassword("Password123");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message")
                                .value("User registered successfully")
                );

        verify(authService)
                .register("test@mail.com", "Password123");
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("Password123");

        when(authService.login(anyString(), anyString()))
                .thenReturn("jwt-token");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data")
                                .value("jwt-token")
                );

        verify(authService)
                .login("test@mail.com", "Password123");
    }

    @Test
    @DisplayName("Should return current authenticated user")
    void shouldReturnCurrentUser() throws Exception {

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@mail.com");

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        user,
                        null,
                        Collections.emptyList()
                );

        authentication.setAuthenticated(true);

        mockMvc.perform(
                        get("/auth/me")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.email")
                                .value("test@mail.com")
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .exists()
                );
    }
}