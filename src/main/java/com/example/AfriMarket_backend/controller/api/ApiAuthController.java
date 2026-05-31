package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.*;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.MomoProvider;
import com.example.AfriMarket_backend.model.enums.UserRole;
import com.example.AfriMarket_backend.model.enums.UserStatus;
import com.example.AfriMarket_backend.repository.UserRepository;
import com.example.AfriMarket_backend.repository.ZoneRepository;
import com.example.AfriMarket_backend.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class ApiAuthController {

    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ApiAuthController(UserRepository userRepository, ZoneRepository zoneRepository,
                              PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req) {
        User user = userRepository.findByPhone(req.getPhone())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Numéro de téléphone ou mot de passe incorrect."));
        }
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Compte suspendu ou désactivé."));
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generate(user.getPhone(),
                Map.of("role", user.getRole().name(), "userId", user.getId()));

        return ResponseEntity.ok(new AuthResponse(token, UserDto.from(user)));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.findByPhone(req.getPhone()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ce numéro de téléphone est déjà utilisé."));
        }

        // Determine role — default CONSUMER if not specified
        UserRole role = UserRole.CONSUMER;
        if ("PRODUCER".equalsIgnoreCase(req.getRole())) role = UserRole.PRODUCER;

        User user = new User();
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);
        // Consumers get immediate access; producers need admin validation
        user.setStatus(role == UserRole.CONSUMER ? UserStatus.ACTIVE : UserStatus.PENDING);
        user.setMomoNumber(req.getMomoNumber() != null ? req.getMomoNumber() : req.getPhone());

        if (req.getMomoProvider() != null) {
            try { user.setMomoProvider(MomoProvider.valueOf(req.getMomoProvider())); }
            catch (Exception ignored) { user.setMomoProvider(MomoProvider.MTN); }
        } else {
            user.setMomoProvider(MomoProvider.MTN);
        }

        if (req.getZoneId() != null) {
            zoneRepository.findById(req.getZoneId()).ifPresent(user::setZone);
        }
        if (req.getArrondissement() != null) {
            user.setArrondissement(req.getArrondissement());
        }

        User saved = userRepository.save(user);
        String token = jwtService.generate(saved.getPhone(),
                Map.of("role", saved.getRole().name(), "userId", saved.getId()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, UserDto.from(saved)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // Reload fresh from DB
        User fresh = userRepository.findById(user.getId()).orElse(user);
        return ResponseEntity.ok(UserDto.from(fresh));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@AuthenticationPrincipal User user,
                                       @RequestBody Map<String, String> body) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User fresh = userRepository.findById(user.getId()).orElse(user);
        if (body.containsKey("fullName")) fresh.setFullName(body.get("fullName"));
        if (body.containsKey("momoNumber")) fresh.setMomoNumber(body.get("momoNumber"));
        if (body.containsKey("momoProvider")) {
            try { fresh.setMomoProvider(MomoProvider.valueOf(body.get("momoProvider"))); }
            catch (Exception ignored) {}
        }
        if (body.containsKey("zoneId")) {
            try {
                Long zoneId = Long.parseLong(body.get("zoneId"));
                zoneRepository.findById(zoneId).ifPresent(fresh::setZone);
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(UserDto.from(userRepository.save(fresh)));
    }
}
