package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.Offer;
import com.example.AfriMarket_backend.model.enums.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    long countByStatus(OfferStatus status);
    List<Offer> findByStatusOrderByCreatedAtAsc(OfferStatus status);
    Page<Offer> findByStatus(OfferStatus status, Pageable pageable);
    Page<Offer> findAll(Pageable pageable);
    List<Offer> findTop5ByOrderByCreatedAtDesc();

    // Producer-specific queries
    List<Offer> findByProducerIdOrderByCreatedAtDesc(Long producerId);
    long countByProducerIdAndStatus(Long producerId, OfferStatus status);

    // Consumer public browsing — ACTIVE + optional filters (native SQLite query)
    @Query(value = "SELECT * FROM offers WHERE status IN ('ACTIVE', 'THRESHOLD_REACHED') " +
                   "AND (:category IS NULL OR category = :category) " +
                   "AND (:search IS NULL OR LOWER(title) LIKE LOWER('%' || :search || '%')) " +
                   "ORDER BY created_at DESC",
           nativeQuery = true)
    List<Offer> findActiveOffers(@Param("category") String category,
                                 @Param("search") String search);
}
