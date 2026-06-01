package com.example.AfriMarket_backend.service;

import com.example.AfriMarket_backend.model.Dispute;
import com.example.AfriMarket_backend.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.AfriMarket_backend.model.enums.DisputeStatus;
import com.example.AfriMarket_backend.repository.DisputeRepository;
import com.example.AfriMarket_backend.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;

    public TransactionService(TransactionRepository transactionRepository, DisputeRepository disputeRepository) {
        this.transactionRepository = transactionRepository;
        this.disputeRepository = disputeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findAll(Pageable pageable) {
        try {
            Page<Transaction> page = transactionRepository.findAll(pageable);
            // Force initialization of lazy associations used in templates
            page.getContent().forEach(tx -> {
                if (tx.getOrder() != null) {
                    try { tx.getOrder().getId(); } catch (Exception ignored) {}
                }
            });
            return page;
        } catch (Exception e) {
            log.error("findAll transactions failed: {}", e.getMessage(), e);
            return org.springframework.data.domain.Page.empty(pageable);
        }
    }

    @Transactional(readOnly = true)
    public List<Dispute> getOpenDisputes() {
        try {
            List<Dispute> disputes = disputeRepository.findByStatusOrderByCreatedAtDesc(DisputeStatus.OPEN);
            // Force init of lazy fields used in template
            disputes.forEach(d -> {
                if (d.getOpenedBy() != null) try { d.getOpenedBy().getFullName(); } catch (Exception ignored) {}
                if (d.getOrder() != null) try { d.getOrder().getTotalAmount(); } catch (Exception ignored) {}
            });
            return disputes;
        } catch (Exception e) {
            log.error("getOpenDisputes failed: {}", e.getMessage(), e);
            return java.util.List.of();
        }
    }

    public BigDecimal getTotalCommissions() {
        try {
            BigDecimal result = transactionRepository.sumTotalCommissions();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Transactional
    public Dispute resolveDispute(Long disputeId, String resolution, boolean refund) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Litige introuvable"));
        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolution(resolution + (refund ? " — REMBOURSEMENT" : " — LIBERATION"));
        dispute.setResolvedAt(LocalDateTime.now());
        return disputeRepository.save(dispute);
    }
}
