package com.example.AfriMarket_backend.service;

import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.UserRole;
import com.example.AfriMarket_backend.model.enums.UserStatus;
import com.example.AfriMarket_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> findUsers(UserRole role, UserStatus status, String search, Pageable pageable) {
        String roleStr   = role   != null ? role.name()   : null;
        String statusStr = status != null ? status.name() : null;
        String searchStr = (search != null && !search.isBlank()) ? search : null;
        return userRepository.findByFilters(roleStr, statusStr, searchStr, pageable);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    @Transactional
    public User updateStatus(Long id, UserStatus newStatus) {
        User user = findById(id);
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
