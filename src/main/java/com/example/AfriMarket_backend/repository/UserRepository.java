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

    @Query(value = "SELECT * FROM users u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR LOWER(u.full_name) LIKE LOWER('%' || :search || '%') OR u.phone LIKE '%' || :search || '%') " +
           "ORDER BY u.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM users u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR LOWER(u.full_name) LIKE LOWER('%' || :search || '%') OR u.phone LIKE '%' || :search || '%')",
           nativeQuery = true)
    Page<User> findByFilters(@Param("role") String role,
                             @Param("status") String status,
                             @Param("search") String search,
                             Pageable pageable);
}
