package com.haryokuncoro.ops.controller;

import com.haryokuncoro.ops.dto.ApiResponse;
import com.haryokuncoro.ops.dto.LoginRequest;
import com.haryokuncoro.ops.dto.RegisterRequest;
import com.haryokuncoro.ops.service.AuthService;
import com.haryokuncoro.ops.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse register(@RequestBody RegisterRequest req, HttpServletRequest servletRequest) {
        authService.register(req.getEmail(), req.getPassword());
        return ResponseUtil.success("User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest req, HttpServletRequest servletRequest) {
        String token = authService.login(req.getEmail(), req.getPassword());
        Map<String, Object> resp = Map.of("token", token);
        return ResponseUtil.success(resp);
    }
}