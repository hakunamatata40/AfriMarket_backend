package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(String phone);

    void deleteByPhone(String phone);
}
