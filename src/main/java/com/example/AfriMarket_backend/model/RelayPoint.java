package com.example.AfriMarket_backend.model;

import com.example.AfriMarket_backend.model.enums.RelayStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "relay_points")
public class RelayPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "manager_name")
    private String managerName;

    private String phone;
    private String address;

    @Column(name = "gps_lat")
    private Double gpsLat;

    @Column(name = "gps_lng")
    private Double gpsLng;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "capacity_kg")
    private Double capacityKg;

    @Column(name = "schedule_json", columnDefinition = "TEXT")
    private String scheduleJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelayStatus status = RelayStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
