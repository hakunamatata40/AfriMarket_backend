package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.Offer;
import com.example.AfriMarket_backend.model.OfferPhoto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OfferResponseDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String categoryLabel;
    private String unit;
    private String unitLabel;
    private BigDecimal pricePerUnit;
    private BigDecimal availableQty;
    private BigDecimal minThreshold;
    private BigDecimal minQtyPerBuyer;
    private BigDecimal currentQtyOrdered;
    private int progressPercent;
    private String status;
    private LocalDate availableFrom;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private List<String> photoUrls;
    private UserDto producer;

    public static OfferResponseDto from(Offer o) {
        OfferResponseDto d = new OfferResponseDto();
        d.id = o.getId();
        d.title = o.getTitle();
        d.description = o.getDescription();
        d.category = o.getCategory() != null ? o.getCategory().name() : null;
        d.categoryLabel = o.getCategory() != null ? o.getCategory().getLabel() : null;
        d.unit = o.getUnit() != null ? o.getUnit().name() : null;
        d.unitLabel = o.getUnit() != null ? o.getUnit().getLabel() : null;
        d.pricePerUnit = o.getPricePerUnit();
        d.availableQty = o.getAvailableQty();
        d.minThreshold = o.getMinThreshold();
        d.minQtyPerBuyer = o.getMinQtyPerBuyer();
        d.currentQtyOrdered = o.getCurrentQtyOrdered();
        d.progressPercent = o.getProgressPercent();
        d.status = o.getStatus() != null ? o.getStatus().name() : null;
        d.availableFrom = o.getAvailableFrom();
        d.expiresAt = o.getExpiresAt();
        d.createdAt = o.getCreatedAt();
        d.photoUrls = o.getPhotos() != null
                ? o.getPhotos().stream().map(OfferPhoto::getUrl).collect(Collectors.toList())
                : List.of();
        d.producer = o.getProducer() != null ? UserDto.from(o.getProducer()) : null;
        return d;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getCategoryLabel() { return categoryLabel; }
    public String getUnit() { return unit; }
    public String getUnitLabel() { return unitLabel; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public BigDecimal getMinThreshold() { return minThreshold; }
    public BigDecimal getMinQtyPerBuyer() { return minQtyPerBuyer; }
    public BigDecimal getCurrentQtyOrdered() { return currentQtyOrdered; }
    public int getProgressPercent() { return progressPercent; }
    public String getStatus() { return status; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getPhotoUrls() { return photoUrls; }
    public UserDto getProducer() { return producer; }
}
