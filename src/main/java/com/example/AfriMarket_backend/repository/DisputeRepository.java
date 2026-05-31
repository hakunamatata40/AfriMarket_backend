package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.Dispute;
import com.example.AfriMarket_backend.model.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    long countByStatus(DisputeStatus status);
    List<Dispute> findByStatusOrderByCreatedAtDesc(DisputeStatus status);
}
