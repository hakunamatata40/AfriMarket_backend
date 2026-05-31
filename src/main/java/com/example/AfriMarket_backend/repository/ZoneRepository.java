package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByCity(String city);
}
