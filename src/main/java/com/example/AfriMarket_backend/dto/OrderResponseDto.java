package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponseDto {
    private Long id;
    private String buyerName;
    private String buyerPhone;
    private BigDecimal qtyOrdered;
    private BigDecimal totalAmount;
    private String status;
    private String paymentProvider;
    private LocalDateTime createdAt;
    private String relayName;

    public static OrderResponseDto from(Order o) {
        OrderResponseDto d = new OrderResponseDto();
        d.id = o.getId();
        if (o.getBuyer() != null) {
            // Anonymize: show only first name + masked phone
            String name = o.getBuyer().getFullName();
            d.buyerName = name != null && name.contains(" ") ? name.split(" ")[0] : name;
            String p = o.getBuyer().getPhone();
            d.buyerPhone = p != null && p.length() > 4 ? p.substring(0, p.length() - 4) + "****" : "****";
        }
        d.qtyOrdered = o.getQtyOrdered();
        d.totalAmount = o.getTotalAmount();
        d.status = o.getStatus() != null ? o.getStatus().name() : null;
        d.paymentProvider = o.getPaymentProvider() != null ? o.getPaymentProvider().name() : null;
        d.createdAt = o.getCreatedAt();
        d.relayName = o.getRelay() != null ? o.getRelay().getName() : null;
        return d;
    }

    public Long getId() { return id; }
    public String getBuyerName() { return buyerName; }
    public String getBuyerPhone() { return buyerPhone; }
    public BigDecimal getQtyOrdered() { return qtyOrdered; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getPaymentProvider() { return paymentProvider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRelayName() { return relayName; }
}
