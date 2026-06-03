package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.*;
import com.example.AfriMarket_backend.model.PasswordResetToken;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.MomoProvider;
import com.example.AfriMarket_backend.model.enums.UserRole;
import com.example.AfriMarket_backend.model.enums.UserStatus;
import com.example.AfriMarket_backend.repository.PasswordResetTokenRepository;
import com.example.AfriMarket_backend.repository.UserRepository;
import com.example.AfriMarket_backend.repository.ZoneRepository;
import com.example.AfriMarket_backend.security.JwtService;
import com.example.AfriMarket_backend.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class ApiAuthController {

    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    public ApiAuthController(UserRepository userRepository, ZoneRepository zoneRepository,
                              PasswordEncoder passwordEncoder, JwtService jwtService,
                              PasswordResetTokenRepository resetTokenRepository,
                              EmailService emailService) {
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.resetTokenRepository = resetTokenRepository;
        this.emailService = emailService;
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
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail().trim().toLowerCase());
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
        if (body.containsKey("email") && !body.get("email").isBlank()) {
            fresh.setEmail(body.get("email").trim().toLowerCase());
        }
        return ResponseEntity.ok(UserDto.from(userRepository.save(fresh)));
    }

    // ─── Password Reset ──────────────────────────────────────────────────────

    /** Step 1: Request OTP — sends it to user's registered email */
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Numéro de téléphone requis."));
        }
        User user = userRepository.findByPhone(phone.trim()).orElse(null);
        if (user == null) {
            // Don't reveal if phone exists — return generic success
            return ResponseEntity.ok(Map.of("message", "Si ce numéro est enregistré, un email a été envoyé."));
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Aucun email associé à ce compte. Contactez le support.",
                "code", "NO_EMAIL"
            ));
        }
        // Invalidate any existing tokens
        resetTokenRepository.deleteByPhone(phone.trim());

        // Generate 6-digit OTP
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        PasswordResetToken token = new PasswordResetToken();
        token.setPhone(phone.trim());
        token.setOtp(otp);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        resetTokenRepository.save(token);

        // Send email
        emailService.sendOtp(user.getEmail(), otp, user.getFullName());

        String maskedEmail = maskEmail(user.getEmail());
        return ResponseEntity.ok(Map.of(
            "message", "Code envoyé à " + maskedEmail,
            "maskedEmail", maskedEmail
        ));
    }

    /** Step 2: Verify OTP */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String otp   = body.get("otp");
        if (phone == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Paramètres manquants."));
        }
        PasswordResetToken token = resetTokenRepository
                .findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(phone.trim())
                .orElse(null);
        if (token == null || !token.getOtp().equals(otp.trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Code incorrect ou expiré."));
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Code expiré. Demandez un nouveau code."));
        }
        return ResponseEntity.ok(Map.of("message", "Code valide.", "phone", phone.trim()));
    }

    /** Step 3: Reset password */
    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String phone       = body.get("phone");
        String otp         = body.get("otp");
        String newPassword = body.get("newPassword");
        if (phone == null || otp == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Données invalides. Le mot de passe doit avoir au moins 6 caractères."));
        }
        PasswordResetToken token = resetTokenRepository
                .findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(phone.trim())
                .orElse(null);
        if (token == null || !token.getOtp().equals(otp.trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Session expirée. Recommencez."));
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Code expiré."));
        }
        User user = userRepository.findByPhone(phone.trim()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        token.setUsed(true);
        resetTokenRepository.save(token);

        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        String local = email.substring(0, at);
        String domain = email.substring(at);
        String masked = local.charAt(0) + "*".repeat(Math.max(local.length() - 2, 1)) + local.charAt(local.length() - 1);
        return masked + domain;
    }
}
