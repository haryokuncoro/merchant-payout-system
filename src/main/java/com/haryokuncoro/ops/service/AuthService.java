package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.entity.User;
import com.haryokuncoro.ops.exception.BadRequestException;
import com.haryokuncoro.ops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service @Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Transactional
    public void register(String email, String password) {
        userRepo.findByEmail(email).ifPresent(u -> {
            throw new BadRequestException("Invalid request");
        });

        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new BadRequestException("Weak password");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setEnabled(true);
        userRepo.save(user);
    }

    public String login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return jwtService.generate(user.getId());
    }

}