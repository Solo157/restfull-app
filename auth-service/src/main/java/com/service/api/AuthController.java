package com.service.api;

import com.service.adapters.BillingAdapterService;
import com.service.config.JwtService;
import com.service.database.User;
import com.service.database.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BillingAdapterService billingAdapter;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

    /**
     * Регистрация пользователя.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setFullName(request.fullName());

        boolean isAccountCreated = billingAdapter.createBillingAccount(user.getUserId());
        if (!isAccountCreated) {
            return ResponseEntity.badRequest().body("User has not been created");
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(new AuthRegisterResponse(savedUser.getUserId()));
    }

    /**
     * Получение токена по пользователю и паролю.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@RequestBody LoginRequest request) {
        String username = request.username();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password())
        );

        User user = userRepository.findByUsername(username).orElseThrow();
        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(new AuthLoginResponse(token));
    }

    /**
     * Проверка токена на корректность.
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isValid(token)) {
                String username = jwtService.extractUsername(token);
                Optional<User> usernameOpt = userRepository.findByUsername(username);
                if (usernameOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not registered");
                }

                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation error: " + e.getMessage());
        }
    }

}
