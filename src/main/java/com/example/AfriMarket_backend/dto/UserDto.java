package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.User;

import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String phone;
    private String fullName;
    private String role;
    private String status;
    private String momoNumber;
    private String momoProvider;
    private String zoneName;
    private Long zoneId;
    private String avatarUrl;
    private Double ratingAvg;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private String arrondissement;
    private String email;

    public static UserDto from(User u) {
        UserDto d = new UserDto();
        d.id = u.getId();
        d.phone = u.getPhone();
        d.fullName = u.getFullName();
        d.role = u.getRole() != null ? u.getRole().name() : null;
        d.status = u.getStatus() != null ? u.getStatus().name() : null;
        d.momoNumber = u.getMomoNumber();
        d.momoProvider = u.getMomoProvider() != null ? u.getMomoProvider().name() : null;
        d.zoneName = u.getZone() != null ? u.getZone().getName() : null;
        d.zoneId = u.getZone() != null ? u.getZone().getId() : null;
        d.avatarUrl = u.getAvatarUrl();
        d.ratingAvg = u.getRatingAvg();
        d.ratingCount = u.getRatingCount();
        d.createdAt = u.getCreatedAt();
        d.arrondissement = u.getArrondissement();
        d.email = u.getEmail();
        return d;
    }

    // Getters
    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getMomoNumber() { return momoNumber; }
    public String getMomoProvider() { return momoProvider; }
    public String getZoneName() { return zoneName; }
    public Long getZoneId() { return zoneId; }
    public String getAvatarUrl() { return avatarUrl; }
    public Double getRatingAvg() { return ratingAvg; }
    public Integer getRatingCount() { return ratingCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getArrondissement() { return arrondissement; }
    public String getEmail() { return email; }
}
