package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.OfferCustomRelay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferCustomRelayRepository extends JpaRepository<OfferCustomRelay, Long> {

    List<OfferCustomRelay> findByOfferId(Long offerId);

    void deleteByIdAndOfferId(Long id, Long offerId);
}
