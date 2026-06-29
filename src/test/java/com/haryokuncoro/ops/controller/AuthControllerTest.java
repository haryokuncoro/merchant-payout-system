package com.haryokuncoro.ops.controller;

import com.haryokuncoro.ops.dto.ApiResponse;
import com.haryokuncoro.ops.dto.LoginRequest;
import com.haryokuncoro.ops.dto.RegisterRequest;
import com.haryokuncoro.ops.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_ShouldReturnToken() {

        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.register("test@example.com", "password123"))
                .thenReturn("jwt-token");

        // Act
        ApiResponse response = authController.register(request, servletRequest);

        // Assert
        verify(authService).register("test@example.com", "password123");

        assertTrue(response.isSuccess());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();

        assertEquals("jwt-token", data.get("token"));
    }

    @Test
    void login_ShouldReturnToken() {

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.login("test@example.com", "password123"))
                .thenReturn("jwt-token");

        // Act
        ApiResponse response = authController.login(request, servletRequest);

        // Assert
        verify(authService).login("test@example.com", "password123");

        assertTrue(response.isSuccess());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();

        assertEquals("jwt-token", data.get("token"));
    }
}