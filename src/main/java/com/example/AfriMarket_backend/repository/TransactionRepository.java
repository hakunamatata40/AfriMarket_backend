package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findAll(Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.commission), 0) FROM Transaction t WHERE t.type = 'RELEASE'")
    BigDecimal sumTotalCommissions();

    @Query(value = "SELECT COALESCE(SUM(t.net_amount), 0) FROM transactions t " +
                   "INNER JOIN orders o ON t.order_id = o.id " +
                   "INNER JOIN offers f ON o.offer_id = f.id " +
                   "WHERE f.producer_id = :producerId AND t.type = 'RELEASE'",
           nativeQuery = true)
    BigDecimal sumReleasedForProducer(@Param("producerId") Long producerId);

    @Query(value = "SELECT COALESCE(SUM(t.net_amount), 0) FROM transactions t " +
                   "INNER JOIN orders o ON t.order_id = o.id " +
                   "INNER JOIN offers f ON o.offer_id = f.id " +
                   "WHERE f.producer_id = :producerId AND t.type = 'RELEASE' " +
                   "AND strftime('%Y-%m', t.created_at) = strftime('%Y-%m', 'now')",
           nativeQuery = true)
    BigDecimal sumMonthRevenueForProducer(@Param("producerId") Long producerId);

    @Query(value = "SELECT t.* FROM transactions t " +
                   "INNER JOIN orders o ON t.order_id = o.id " +
                   "INNER JOIN offers f ON o.offer_id = f.id " +
                   "WHERE f.producer_id = :producerId " +
                   "ORDER BY t.created_at DESC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<Transaction> findForProducer(@Param("producerId") Long producerId,
                                       @Param("limit") int limit,
                                       @Param("offset") int offset);
}
