package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.entity.User;
import com.haryokuncoro.ops.exception.BadRequestException;
import com.haryokuncoro.ops.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {

        String email = "test@mail.com";
        String password = "Password123";

        when(userRepo.findByEmail(email))
                .thenReturn(Optional.empty());

        when(encoder.encode(password))
                .thenReturn("encoded-password");

        authService.register(email, password);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepo).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(email, savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertTrue(savedUser.isEnabled());
        assertNotNull(savedUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {

        String email = "test@mail.com";
        String password = "Password123";

        User existingUser = new User();

        when(userRepo.findByEmail(email))
                .thenReturn(Optional.of(existingUser));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(email, password)
        );

        assertEquals(
                "Invalid request",
                exception.getMessage()
        );

        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for weak password")
    void shouldThrowExceptionForWeakPassword() {

        String email = "test@mail.com";
        String password = "weak";

        when(userRepo.findByEmail(email))
                .thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(email, password)
        );

        assertEquals(
                "Weak password",
                exception.getMessage()
        );

        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("test@mail.com");
        user.setPassword("encoded-password");

        when(userRepo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(encoder.matches("Password123", "encoded-password"))
                .thenReturn(true);

        when(jwtService.generate(userId))
                .thenReturn("jwt-token");

        String token = authService.login(
                "test@mail.com",
                "Password123"
        );

        assertEquals("jwt-token", token);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepo.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(
                        "test@mail.com",
                        "Password123"
                )
        );

        assertEquals(
                "User not found",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid password")
    void shouldThrowExceptionForInvalidPassword() {

        User user = new User();
        user.setPassword("encoded-password");

        when(userRepo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(encoder.matches(
                "wrong-password",
                "encoded-password"
        )).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(
                        "test@mail.com",
                        "wrong-password"
                )
        );

        assertEquals(
                "Invalid password",
                exception.getMessage()
        );
    }
}