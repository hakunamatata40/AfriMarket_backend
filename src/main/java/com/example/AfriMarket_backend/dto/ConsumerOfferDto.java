package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.Offer;
import com.example.AfriMarket_backend.model.OfferPhoto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ConsumerOfferDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String categoryLabel;
    private BigDecimal pricePerUnit;
    private String unit;
    private String unitLabel;
    private BigDecimal availableQty;
    private BigDecimal minThreshold;
    private BigDecimal currentQty;
    private BigDecimal minQtyPerBuyer;
    private int progressPercent;
    private String status;
    private String producerName;
    private Long producerId;
    private String producerAvatar;
    private double producerRating;
    private int producerRatingCount;
    private boolean producerVerified;
    private String zoneName;
    private List<String> photos;
    private LocalDate deliveryDate;
    private LocalDateTime expiresAt;
    private int participantsCount;

    public static ConsumerOfferDto from(Offer o) {
        ConsumerOfferDto d = new ConsumerOfferDto();
        d.id = o.getId();
        d.title = o.getTitle();
        d.description = o.getDescription();
        d.category = o.getCategory() != null ? o.getCategory().name() : null;
        d.categoryLabel = o.getCategory() != null ? o.getCategory().getLabel() : null;
        d.pricePerUnit = o.getPricePerUnit();
        d.unit = o.getUnit() != null ? o.getUnit().name() : null;
        d.unitLabel = o.getUnit() != null ? o.getUnit().getLabel() : null;
        d.availableQty = o.getAvailableQty();
        d.minThreshold = o.getMinThreshold();
        d.currentQty = o.getCurrentQtyOrdered() != null ? o.getCurrentQtyOrdered() : BigDecimal.ZERO;
        d.minQtyPerBuyer = o.getMinQtyPerBuyer();
        d.progressPercent = o.getProgressPercent();
        d.status = o.getStatus() != null ? o.getStatus().name() : null;

        if (o.getProducer() != null) {
            d.producerName = o.getProducer().getFullName();
            d.producerId = o.getProducer().getId();
            d.producerAvatar = o.getProducer().getAvatarUrl();
            d.producerRating = o.getProducer().getRatingAvg() != null ? o.getProducer().getRatingAvg() : 0.0;
            d.producerRatingCount = o.getProducer().getRatingCount() != null ? o.getProducer().getRatingCount() : 0;
            d.producerVerified = o.getProducer().getStatus() != null &&
                    o.getProducer().getStatus().name().equals("ACTIVE");
            d.zoneName = o.getProducer().getZone() != null ? o.getProducer().getZone().getName() : null;
        }

        d.photos = o.getPhotos() != null
                ? o.getPhotos().stream().map(OfferPhoto::getUrl).collect(Collectors.toList())
                : List.of();

        d.deliveryDate = o.getAvailableFrom();
        d.expiresAt = o.getExpiresAt();
        return d;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getCategoryLabel() { return categoryLabel; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public String getUnit() { return unit; }
    public String getUnitLabel() { return unitLabel; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public BigDecimal getMinThreshold() { return minThreshold; }
    public BigDecimal getCurrentQty() { return currentQty; }
    public BigDecimal getMinQtyPerBuyer() { return minQtyPerBuyer; }
    public int getProgressPercent() { return progressPercent; }
    public String getStatus() { return status; }
    public String getProducerName() { return producerName; }
    public Long getProducerId() { return producerId; }
    public String getProducerAvatar() { return producerAvatar; }
    public double getProducerRating() { return producerRating; }
    public int getProducerRatingCount() { return producerRatingCount; }
    public boolean isProducerVerified() { return producerVerified; }
    public String getZoneName() { return zoneName; }
    public List<String> getPhotos() { return photos; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int n) { this.participantsCount = n; }
}
