package com.example.AfriMarket_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank private String fullName;
    @NotBlank private String phone;
    @NotBlank private String password;
    private String momoNumber;
    private String momoProvider; // MTN or ORANGE
    private Long zoneId;
    private String role;         // CONSUMER or PRODUCER (default: CONSUMER)
    private String arrondissement; // For consumers: preferred district

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMomoNumber() { return momoNumber; }
    public void setMomoNumber(String momoNumber) { this.momoNumber = momoNumber; }
    public String getMomoProvider() { return momoProvider; }
    public void setMomoProvider(String momoProvider) { this.momoProvider = momoProvider; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getArrondissement() { return arrondissement; }
    public void setArrondissement(String arrondissement) { this.arrondissement = arrondissement; }

    private String email; // Optional — used for password reset OTP
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
