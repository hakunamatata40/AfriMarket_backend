package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.Order;
import com.example.AfriMarket_backend.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") OrderStatus status);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED' AND date(completed_at) = date('now')",
           nativeQuery = true)
    BigDecimal sumTodayRevenue();

    List<Order> findTop10ByOrderByCreatedAtDesc();

    Page<Order> findAll(Pageable pageable);

    List<Order> findByOfferIdOrderByCreatedAtDesc(Long offerId);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o " +
                   "INNER JOIN offers f ON o.offer_id = f.id " +
                   "WHERE f.producer_id = :producerId AND o.status IN ('PAID','CONFIRMED','DELIVERING','AT_RELAY')",
           nativeQuery = true)
    BigDecimal sumPendingEscrowForProducer(@Param("producerId") Long producerId);

    // Consumer queries
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    boolean existsByBuyerIdAndOfferId(Long buyerId, Long offerId);
}
