package com.example.AfriMarket_backend.service;

import com.example.AfriMarket_backend.model.Offer;
import com.example.AfriMarket_backend.model.enums.OfferStatus;
import com.example.AfriMarket_backend.repository.OfferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public List<Offer> getPendingOffers() {
        return offerRepository.findByStatusOrderByCreatedAtAsc(OfferStatus.PENDING_REVIEW);
    }

    public Page<Offer> findAll(Pageable pageable) {
        return offerRepository.findAll(pageable);
    }

    public Offer findById(Long id) {
        return offerRepository.findById(id).orElseThrow(() -> new RuntimeException("Offre introuvable"));
    }

    @Transactional
    public Offer approve(Long id) {
        Offer offer = findById(id);
        offer.setStatus(OfferStatus.ACTIVE);
        offer.setRejectionReason(null);
        return offerRepository.save(offer);
    }

    @Transactional
    public Offer reject(Long id, String reason) {
        Offer offer = findById(id);
        offer.setStatus(OfferStatus.CANCELLED);
        offer.setRejectionReason(reason);
        return offerRepository.save(offer);
    }

    @Transactional
    public Offer forceClose(Long id) {
        Offer offer = findById(id);
        offer.setStatus(OfferStatus.CANCELLED);
        return offerRepository.save(offer);
    }
}
