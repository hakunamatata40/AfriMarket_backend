package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.UserRole;
import com.example.AfriMarket_backend.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    List<User> findTop10ByOrderByCreatedAtDesc();
    long countByRole(UserRole role);
    long countByStatus(UserStatus status);
    long countByRoleAndStatus(UserRole role, UserStatus status);

    @Query(value = "SELECT * FROM users WHERE " +
           "(:role IS NULL OR role = :role) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:search IS NULL OR (LOWER(full_name) LIKE LOWER('%' || :search || '%') OR phone LIKE '%' || :search || '%')) " +
           "ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(*) FROM users WHERE " +
           "(:role IS NULL OR role = :role) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:search IS NULL OR (LOWER(full_name) LIKE LOWER('%' || :search || '%') OR phone LIKE '%' || :search || '%'))",
           nativeQuery = true)
    Page<User> findByFilters(@Param("role") String role,
                             @Param("status") String status,
                             @Param("search") String search,
                             Pageable pageable);
}
