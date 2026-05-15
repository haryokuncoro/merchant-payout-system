package com.haryokuncoro.ops.controller;

import com.haryokuncoro.ops.dto.ApiResponse;
import com.haryokuncoro.ops.dto.LoginRequest;
import com.haryokuncoro.ops.dto.RegisterRequest;
import com.haryokuncoro.ops.entity.User;
import com.haryokuncoro.ops.service.AuthService;
import com.haryokuncoro.ops.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest req, HttpServletRequest servletRequest) {
        authService.register(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(
                ResponseUtil.success("User registered successfully")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest req, HttpServletRequest servletRequest) {
        String token = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(
                ResponseUtil.success("", token)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map>> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Map<String, Object> map =  Map.of(
                "id", user.getId(),
                "email", user.getEmail()
        );
        return ResponseEntity.ok(
                ResponseUtil.success("", map)
        );
    }
}