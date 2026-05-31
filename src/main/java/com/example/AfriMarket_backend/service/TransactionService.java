package com.example.AfriMarket_backend.service;

import com.example.AfriMarket_backend.model.Dispute;
import com.example.AfriMarket_backend.model.Transaction;
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

    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;

    public TransactionService(TransactionRepository transactionRepository, DisputeRepository disputeRepository) {
        this.transactionRepository = transactionRepository;
        this.disputeRepository = disputeRepository;
    }

    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public List<Dispute> getOpenDisputes() {
        return disputeRepository.findByStatusOrderByCreatedAtDesc(DisputeStatus.OPEN);
    }

    public BigDecimal getTotalCommissions() {
        return transactionRepository.sumTotalCommissions();
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
