package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.RelayPoint;
import com.example.AfriMarket_backend.model.enums.RelayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelayPointRepository extends JpaRepository<RelayPoint, Long> {
    List<RelayPoint> findByStatus(RelayStatus status);
    long countByStatus(RelayStatus status);
}
