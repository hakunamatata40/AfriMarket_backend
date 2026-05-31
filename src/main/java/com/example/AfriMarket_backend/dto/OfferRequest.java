package com.example.AfriMarket_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OfferRequest {
    @NotBlank private String title;
    private String description;
    @NotNull private String category;   // OfferCategory name
    @NotNull private String unit;       // OfferUnit name
    @NotNull private BigDecimal pricePerUnit;
    @NotNull private BigDecimal availableQty;
    @NotNull private BigDecimal minThreshold;
    @NotNull private BigDecimal minQtyPerBuyer;
    private LocalDate availableFrom;
    private Integer validDays;          // days until expiry, default 7
    private List<Long> zoneIds;
    private List<Long> relayIds;

    // Getters / setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }
    public BigDecimal getMinQtyPerBuyer() { return minQtyPerBuyer; }
    public void setMinQtyPerBuyer(BigDecimal minQtyPerBuyer) { this.minQtyPerBuyer = minQtyPerBuyer; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public List<Long> getZoneIds() { return zoneIds; }
    public void setZoneIds(List<Long> zoneIds) { this.zoneIds = zoneIds; }
    public List<Long> getRelayIds() { return relayIds; }
    public void setRelayIds(List<Long> relayIds) { this.relayIds = relayIds; }
}
