package com.example.AfriMarket_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;
    private String region;

    public Zone() {}
    public Zone(String name, String city, String region) {
        this.name = name;
        this.city = city;
        this.region = region;
    }
}
