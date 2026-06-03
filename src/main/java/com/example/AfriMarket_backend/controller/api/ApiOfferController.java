package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.OfferRequest;
import com.example.AfriMarket_backend.dto.OfferResponseDto;
import com.example.AfriMarket_backend.dto.OrderResponseDto;
import com.example.AfriMarket_backend.model.Offer;
import com.example.AfriMarket_backend.model.OfferCustomRelay;
import com.example.AfriMarket_backend.model.OfferPhoto;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.OfferCategory;
import com.example.AfriMarket_backend.model.enums.OfferStatus;
import com.example.AfriMarket_backend.model.enums.OfferUnit;
import com.example.AfriMarket_backend.repository.OfferCustomRelayRepository;
import com.example.AfriMarket_backend.repository.OfferRepository;
import com.example.AfriMarket_backend.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/offers")
public class ApiOfferController {

    private final OfferRepository offerRepository;
    private final OrderRepository orderRepository;
    private final OfferCustomRelayRepository customRelayRepository;

    public ApiOfferController(OfferRepository offerRepository, OrderRepository orderRepository,
                               OfferCustomRelayRepository customRelayRepository) {
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.customRelayRepository = customRelayRepository;
    }

    @GetMapping("/mine")
    public ResponseEntity<?> myOffers(@AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        List<Offer> offers = offerRepository.findByProducerIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(offers.stream().map(OfferResponseDto::from).collect(Collectors.toList()));
    }

    // GET /api/v1/offers/{id} is handled by ApiConsumerController (public view for all roles).
    // Producers access their offer details through /mine list or the consumer endpoint.

    @PostMapping
    public ResponseEntity<?> createOffer(@Valid @RequestBody OfferRequest req,
                                          @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();

        Offer offer = new Offer();
        offer.setProducer(user);
        offer.setTitle(req.getTitle());
        offer.setDescription(req.getDescription());
        offer.setStatus(OfferStatus.DRAFT);

        try {
            offer.setCategory(OfferCategory.valueOf(req.getCategory()));
            offer.setUnit(OfferUnit.valueOf(req.getUnit()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Catégorie ou unité invalide."));
        }

        offer.setPricePerUnit(req.getPricePerUnit());
        offer.setAvailableQty(req.getAvailableQty());
        offer.setMinThreshold(req.getMinThreshold());
        offer.setMinQtyPerBuyer(req.getMinQtyPerBuyer());
        offer.setCurrentQtyOrdered(BigDecimal.ZERO);
        offer.setAvailableFrom(req.getAvailableFrom() != null ? req.getAvailableFrom() : LocalDate.now().plusDays(3));
        int days = req.getValidDays() != null ? req.getValidDays() : 7;
        offer.setExpiresAt(LocalDateTime.now().plusDays(days));

        Offer saved = offerRepository.save(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(OfferResponseDto.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOffer(@PathVariable Long id,
                                          @RequestBody OfferRequest req,
                                          @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();
        if (offer.getStatus() != OfferStatus.DRAFT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seuls les brouillons peuvent être modifiés."));
        }

        if (req.getTitle() != null) offer.setTitle(req.getTitle());
        if (req.getDescription() != null) offer.setDescription(req.getDescription());
        if (req.getPricePerUnit() != null) offer.setPricePerUnit(req.getPricePerUnit());
        if (req.getAvailableQty() != null) offer.setAvailableQty(req.getAvailableQty());
        if (req.getMinThreshold() != null) offer.setMinThreshold(req.getMinThreshold());
        if (req.getMinQtyPerBuyer() != null) offer.setMinQtyPerBuyer(req.getMinQtyPerBuyer());
        if (req.getAvailableFrom() != null) offer.setAvailableFrom(req.getAvailableFrom());
        if (req.getCategory() != null) {
            try { offer.setCategory(OfferCategory.valueOf(req.getCategory())); } catch (Exception ignored) {}
        }
        if (req.getUnit() != null) {
            try { offer.setUnit(OfferUnit.valueOf(req.getUnit())); } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(OfferResponseDto.from(offerRepository.save(offer)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();
        if (offer.getStatus() != OfferStatus.DRAFT) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'offre n'est pas un brouillon."));
        }
        offer.setStatus(OfferStatus.PENDING_REVIEW);
        return ResponseEntity.ok(OfferResponseDto.from(offerRepository.save(offer)));
    }

    @PostMapping("/{id}/confirm-delivery")
    public ResponseEntity<?> confirmDelivery(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();
        if (offer.getStatus() != OfferStatus.THRESHOLD_REACHED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le seuil n'est pas encore atteint."));
        }
        offer.setStatus(OfferStatus.DELIVERING);
        return ResponseEntity.ok(OfferResponseDto.from(offerRepository.save(offer)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOffer(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();
        offer.setStatus(OfferStatus.CANCELLED);
        offerRepository.save(offer);
        return ResponseEntity.ok(Map.of("message", "Offre annulée."));
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();

        try {
            String uploadDir = "uploads/offers/";
            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            String url = "/uploads/offers/" + filename;
            OfferPhoto photo = new OfferPhoto();
            photo.setOffer(offer);
            photo.setUrl(url);
            photo.setSortOrder(offer.getPhotos().size());
            offer.getPhotos().add(photo);
            offerRepository.save(offer);

            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'upload."));
        }
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<?> getOrders(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();

        List<com.example.AfriMarket_backend.model.Order> orders =
                orderRepository.findByOfferIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(orders.stream().map(OrderResponseDto::from).collect(Collectors.toList()));
    }

    // ─── Custom relay endpoints ───────────────────────────────────────────────

    @GetMapping("/{id}/custom-relays")
    public ResponseEntity<?> listCustomRelays(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();

        List<OfferCustomRelay> relays = customRelayRepository.findByOfferId(id);
        List<Map<String, Object>> result = relays.stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("address", r.getAddress() != null ? r.getAddress() : "");
            m.put("lat", r.getLat() != null ? r.getLat() : 0.0);
            m.put("lng", r.getLng() != null ? r.getLng() : 0.0);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/custom-relays")
    public ResponseEntity<?> addCustomRelay(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();

        String name = body.get("name") != null ? body.get("name").toString() : null;
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nom est requis."));
        }

        OfferCustomRelay relay = new OfferCustomRelay();
        relay.setOffer(offer);
        relay.setName(name);
        relay.setAddress(body.get("address") != null ? body.get("address").toString() : null);
        try { relay.setLat(body.get("lat") != null ? Double.parseDouble(body.get("lat").toString()) : null); } catch (Exception ignored) {}
        try { relay.setLng(body.get("lng") != null ? Double.parseDouble(body.get("lng").toString()) : null); } catch (Exception ignored) {}

        OfferCustomRelay saved = customRelayRepository.save(relay);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("id", saved.getId());
        resp.put("name", saved.getName());
        resp.put("address", saved.getAddress() != null ? saved.getAddress() : "");
        resp.put("lat", saved.getLat() != null ? saved.getLat() : 0.0);
        resp.put("lng", saved.getLng() != null ? saved.getLng() : 0.0);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @DeleteMapping("/{id}/custom-relays/{relayId}")
    public ResponseEntity<?> deleteCustomRelay(@PathVariable Long id,
                                                @PathVariable Long relayId,
                                                @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();
        if (!offer.getProducer().getId().equals(user.getId())) return forbidden();

        customRelayRepository.deleteByIdAndOfferId(relayId, id);
        return ResponseEntity.ok(Map.of("message", "Relais supprimé."));
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié."));
    }
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Accès refusé."));
    }
}
