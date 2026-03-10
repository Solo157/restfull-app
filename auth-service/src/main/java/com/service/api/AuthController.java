package com.service.api;

import com.service.adapters.BillingAdapter;
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
    private final BillingAdapter billingAdapter;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> met() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setEmail(req.email());
        user.setFullName(req.fullName());

        boolean isAccountCreated = billingAdapter.createAccount(user.getUserId());
        if (!isAccountCreated) {
            return ResponseEntity.badRequest().body("User has not been created");
        }

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        User user = userRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
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
