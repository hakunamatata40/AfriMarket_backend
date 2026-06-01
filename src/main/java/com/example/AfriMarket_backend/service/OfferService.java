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

    @Transactional(readOnly = true)
    public List<Offer> getPendingOffers() {
        List<Offer> offers = offerRepository.findByStatusOrderByCreatedAtAsc(OfferStatus.PENDING_REVIEW);
        // Force-load lazy associations used in the template
        offers.forEach(o -> {
            if (o.getProducer() != null) try { o.getProducer().getFullName(); o.getProducer().getZone(); } catch (Exception ignored) {}
            if (o.getPhotos() != null)   try { o.getPhotos().size(); } catch (Exception ignored) {}
        });
        return offers;
    }

    @Transactional(readOnly = true)
    public Page<Offer> findAll(Pageable pageable) {
        Page<Offer> page = offerRepository.findAll(pageable);
        // Force-load lazy associations
        page.getContent().forEach(o -> {
            if (o.getProducer() != null) try { o.getProducer().getFullName(); } catch (Exception ignored) {}
        });
        return page;
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
