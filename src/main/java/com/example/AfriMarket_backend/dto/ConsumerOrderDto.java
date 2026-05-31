package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ConsumerOrderDto {
    private Long id;
    private Long offerId;
    private String offerTitle;
    private String offerPhoto;
    private String producerName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;
    private String status;
    private String relayName;
    private String relayAddress;
    private LocalDateTime deliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConsumerOrderDto from(Order o) {
        ConsumerOrderDto d = new ConsumerOrderDto();
        d.id = o.getId();

        if (o.getOffer() != null) {
            d.offerId = o.getOffer().getId();
            d.offerTitle = o.getOffer().getTitle();
            d.unit = o.getOffer().getUnit() != null ? o.getOffer().getUnit().getLabel() : null;
            d.pricePerUnit = o.getUnitPriceSnapshot();
            // First photo URL
            if (o.getOffer().getPhotos() != null && !o.getOffer().getPhotos().isEmpty()) {
                d.offerPhoto = o.getOffer().getPhotos().get(0).getUrl();
            }
            if (o.getOffer().getProducer() != null) {
                d.producerName = o.getOffer().getProducer().getFullName();
            }
        }

        d.quantity = o.getQtyOrdered();
        d.totalPrice = o.getTotalAmount();
        d.status = o.getStatus() != null ? o.getStatus().name() : null;

        if (o.getRelay() != null) {
            d.relayName = o.getRelay().getName();
            d.relayAddress = o.getRelay().getAddress();
        }

        d.deliveryDate = o.getDeliveredAt();
        d.createdAt = o.getCreatedAt();
        // Use completedAt or deliveredAt as updatedAt
        d.updatedAt = o.getCompletedAt() != null ? o.getCompletedAt()
                    : o.getDeliveredAt() != null ? o.getDeliveredAt()
                    : o.getCreatedAt();
        return d;
    }

    // Getters
    public Long getId() { return id; }
    public Long getOfferId() { return offerId; }
    public String getOfferTitle() { return offerTitle; }
    public String getOfferPhoto() { return offerPhoto; }
    public String getProducerName() { return producerName; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getRelayName() { return relayName; }
    public String getRelayAddress() { return relayAddress; }
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
