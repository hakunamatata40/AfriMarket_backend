package com.example.AfriMarket_backend.model;

import com.example.AfriMarket_backend.model.enums.MomoProvider;
import com.example.AfriMarket_backend.model.enums.UserRole;
import com.example.AfriMarket_backend.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String phone;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CONSUMER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "momo_number")
    private String momoNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "momo_provider")
    private MomoProvider momoProvider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "id_doc_url")
    private String idDocUrl;

    @Column(name = "rating_avg")
    private Double ratingAvg = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Username field used for admin login
    @Column(unique = true)
    private String username;

    // Consumer preferred delivery district (e.g. "Yaoundé 4")
    @Column(name = "arrondissement")
    private String arrondissement;

    // Optional email — used for password reset OTP
    // Note: unique constraint omitted — SQLite cannot add UNIQUE via ALTER TABLE
    @Column(name = "email")
    private String email;
}
