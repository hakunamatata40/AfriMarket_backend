package com.example.AfriMarket_backend.model;

import com.example.AfriMarket_backend.model.enums.OfferCategory;
import com.example.AfriMarket_backend.model.enums.OfferStatus;
import com.example.AfriMarket_backend.model.enums.OfferUnit;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "offers")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producer_id", nullable = false)
    private User producer;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferUnit unit;

    @Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "available_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal availableQty;

    @Column(name = "min_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal minThreshold;

    @Column(name = "min_qty_per_buyer", nullable = false, precision = 10, scale = 2)
    private BigDecimal minQtyPerBuyer;

    @Column(name = "current_qty_ordered", precision = 10, scale = 2)
    private BigDecimal currentQtyOrdered = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.DRAFT;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OfferPhoto> photos = new ArrayList<>();

    // Progress percentage
    @Transient
    public int getProgressPercent() {
        if (minThreshold == null || minThreshold.compareTo(BigDecimal.ZERO) == 0) return 0;
        BigDecimal pct = currentQtyOrdered.divide(minThreshold, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return Math.min(100, pct.intValue());
    }
}
