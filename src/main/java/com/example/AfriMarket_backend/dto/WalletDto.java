package com.example.AfriMarket_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class WalletDto {
    private BigDecimal disponible;
    private BigDecimal enAttente;
    private List<TransactionDto> recentTransactions;

    public WalletDto(BigDecimal disponible, BigDecimal enAttente, List<TransactionDto> recentTransactions) {
        this.disponible = disponible;
        this.enAttente = enAttente;
        this.recentTransactions = recentTransactions;
    }

    public BigDecimal getDisponible() { return disponible; }
    public BigDecimal getEnAttente() { return enAttente; }
    public List<TransactionDto> getRecentTransactions() { return recentTransactions; }

    public static class TransactionDto {
        private Long id;
        private String type;
        private java.math.BigDecimal amount;
        private java.math.BigDecimal commission;
        private java.math.BigDecimal netAmount;
        private String status;
        private java.time.LocalDateTime createdAt;
        private String orderRef;

        public static TransactionDto from(com.example.AfriMarket_backend.model.Transaction t) {
            TransactionDto d = new TransactionDto();
            d.id = t.getId();
            d.type = t.getType() != null ? t.getType().name() : null;
            d.amount = t.getAmount();
            d.commission = t.getCommission();
            d.netAmount = t.getNetAmount();
            d.status = t.getStatus();
            d.createdAt = t.getCreatedAt();
            d.orderRef = t.getOrder() != null ? "#" + t.getOrder().getId() : null;
            return d;
        }

        public Long getId() { return id; }
        public String getType() { return type; }
        public java.math.BigDecimal getAmount() { return amount; }
        public java.math.BigDecimal getCommission() { return commission; }
        public java.math.BigDecimal getNetAmount() { return netAmount; }
        public String getStatus() { return status; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public String getOrderRef() { return orderRef; }
    }
}
