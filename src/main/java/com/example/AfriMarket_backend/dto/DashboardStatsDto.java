package com.example.AfriMarket_backend.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private long activeOffers;
    private long pendingOffers;
    private long completedOffers;
    private BigDecimal monthRevenue;
    private BigDecimal pendingEscrow;
    private Double ratingAvg;
    private Integer ratingCount;

    public DashboardStatsDto(long activeOffers, long pendingOffers, long completedOffers,
                              BigDecimal monthRevenue, BigDecimal pendingEscrow,
                              Double ratingAvg, Integer ratingCount) {
        this.activeOffers = activeOffers;
        this.pendingOffers = pendingOffers;
        this.completedOffers = completedOffers;
        this.monthRevenue = monthRevenue;
        this.pendingEscrow = pendingEscrow;
        this.ratingAvg = ratingAvg;
        this.ratingCount = ratingCount;
    }

    public long getActiveOffers() { return activeOffers; }
    public long getPendingOffers() { return pendingOffers; }
    public long getCompletedOffers() { return completedOffers; }
    public BigDecimal getMonthRevenue() { return monthRevenue; }
    public BigDecimal getPendingEscrow() { return pendingEscrow; }
    public Double getRatingAvg() { return ratingAvg; }
    public Integer getRatingCount() { return ratingCount; }
}
