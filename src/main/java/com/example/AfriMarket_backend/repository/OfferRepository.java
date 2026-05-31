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

    // Consumer public browsing — ACTIVE + optional filters
    @Query("SELECT o FROM Offer o WHERE o.status IN ('ACTIVE', 'THRESHOLD_REACHED') " +
           "AND (:category IS NULL OR CAST(o.category AS string) = :category) " +
           "AND (:search IS NULL OR LOWER(o.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY o.createdAt DESC")
    List<Offer> findActiveOffers(@Param("category") String category,
                                 @Param("search") String search);
}
