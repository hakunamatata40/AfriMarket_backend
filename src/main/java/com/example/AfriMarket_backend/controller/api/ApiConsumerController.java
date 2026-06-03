package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.ConsumerOfferDto;
import com.example.AfriMarket_backend.dto.ConsumerOrderDto;
import com.example.AfriMarket_backend.model.Offer;
import com.example.AfriMarket_backend.model.Order;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.MomoProvider;
import com.example.AfriMarket_backend.model.enums.OfferStatus;
import com.example.AfriMarket_backend.model.enums.OrderStatus;
import com.example.AfriMarket_backend.repository.*;
import com.example.AfriMarket_backend.repository.OfferCustomRelayRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consumer-facing endpoints:
 *  GET  /api/v1/offers          — browse active offers
 *  GET  /api/v1/offers/{id}     — offer detail (public)
 *  POST /api/v1/offers/{id}/join — join a groupage
 *  GET  /api/v1/orders/mine     — consumer's orders
 *  POST /api/v1/orders/{id}/confirm-receipt
 *  POST /api/v1/orders/{id}/dispute
 */
@RestController
public class ApiConsumerController {

    private final OfferRepository offerRepository;
    private final OrderRepository orderRepository;
    private final RelayPointRepository relayRepository;
    private final OfferCustomRelayRepository customRelayRepo;

    public ApiConsumerController(OfferRepository offerRepository,
                                  OrderRepository orderRepository,
                                  RelayPointRepository relayRepository,
                                  OfferCustomRelayRepository customRelayRepo) {
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.relayRepository = relayRepository;
        this.customRelayRepo = customRelayRepo;
    }

    // ─── Browse active offers ────────────────────────────────────────────────

    @GetMapping("/api/v1/offers")
    public ResponseEntity<?> getActiveOffers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User user
    ) {
        // This endpoint is accessible to authenticated users (any role)
        if (user == null) return unauthorized();

        List<Offer> offers = offerRepository.findActiveOffers(
                category != null && !category.isBlank() ? category : null,
                search != null && !search.isBlank() ? search : null
        );

        List<ConsumerOfferDto> dtos = offers.stream().map(o -> {
            ConsumerOfferDto dto = ConsumerOfferDto.from(o);
            // Inject participant count
            int count = orderRepository.findByOfferIdOrderByCreatedAtDesc(o.getId()).size();
            dto.setParticipantsCount(count);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ─── Single offer detail ─────────────────────────────────────────────────

    @GetMapping("/api/v1/offers/{id}")
    public ResponseEntity<?> getOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) return unauthorized();

        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();

        // Producers can only see their own via /offers/mine; public view for consumers
        ConsumerOfferDto dto = ConsumerOfferDto.from(offer);
        int count = orderRepository.findByOfferIdOrderByCreatedAtDesc(id).size();
        dto.setParticipantsCount(count);

        // Get active relays
        List<java.util.Map<String, Object>> relays = relayRepository.findAll().stream()
                .filter(r -> r.getStatus() != null &&
                        r.getStatus().name().equals("ACTIVE"))
                .map(r -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", String.valueOf(r.getId()));
                    m.put("name", r.getName() != null ? r.getName() : "");
                    m.put("address", r.getAddress() != null ? r.getAddress() : "");
                    m.put("zoneName", r.getZone() != null ? r.getZone().getName() : "");
                    m.put("distance", "0");
                    return m;
                })
                .collect(Collectors.toList());

        List<java.util.Map<String, Object>> customRelays = customRelayRepo.findByOfferId(id).stream()
                .map(r -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", "custom_" + r.getId());
                    m.put("name", r.getName());
                    m.put("address", r.getAddress() != null ? r.getAddress() : "");
                    m.put("zoneName", "Point producteur");
                    m.put("lat", r.getLat() != null ? r.getLat() : 0.0);
                    m.put("lng", r.getLng() != null ? r.getLng() : 0.0);
                    m.put("distance", "0");
                    m.put("isCustom", true);
                    return m;
                })
                .collect(Collectors.toList());

        var allRelays = new java.util.ArrayList<>(relays);
        allRelays.addAll(customRelays);

        return ResponseEntity.ok(Map.of("offer", dto, "relays", allRelays));
    }

    // ─── Join an offer ───────────────────────────────────────────────────────

    @PostMapping("/api/v1/offers/{id}/join")
    public ResponseEntity<?> joinOffer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) return unauthorized();

        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) return ResponseEntity.notFound().build();

        if (offer.getStatus() != OfferStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cette offre n'accepte plus de nouvelles commandes."));
        }

        // Parse quantity
        BigDecimal qty;
        try {
            qty = new BigDecimal(body.get("quantity").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Quantité invalide."));
        }

        if (offer.getMinQtyPerBuyer() != null && qty.compareTo(offer.getMinQtyPerBuyer()) < 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Quantité minimum : " + offer.getMinQtyPerBuyer() + " " +
                              (offer.getUnit() != null ? offer.getUnit().getLabel() : "")
            ));
        }

        // Check available remaining quantity
        BigDecimal remaining = offer.getAvailableQty().subtract(
                offer.getCurrentQtyOrdered() != null ? offer.getCurrentQtyOrdered() : BigDecimal.ZERO
        );
        if (qty.compareTo(remaining) > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Quantité demandée (" + qty + ") supérieure au stock restant (" + remaining + ")."
            ));
        }

        // Relay selection (optional)
        Order order = new Order();
        order.setOffer(offer);
        order.setBuyer(user);
        order.setQtyOrdered(qty);
        order.setUnitPriceSnapshot(offer.getPricePerUnit());
        order.setTotalAmount(offer.getPricePerUnit().multiply(qty));
        order.setStatus(OrderStatus.PAID); // Simulated payment
        order.setPaymentProvider(MomoProvider.MTN);

        if (body.get("relayId") != null) {
            try {
                Long relayId = Long.parseLong(body.get("relayId").toString());
                relayRepository.findById(relayId).ifPresent(order::setRelay);
            } catch (Exception ignored) {}
        }

        Order saved = orderRepository.save(order);

        // Update offer's current quantity ordered
        BigDecimal newCurrent = (offer.getCurrentQtyOrdered() != null ? offer.getCurrentQtyOrdered() : BigDecimal.ZERO)
                .add(qty);
        offer.setCurrentQtyOrdered(newCurrent);

        // Check if threshold reached
        if (newCurrent.compareTo(offer.getMinThreshold()) >= 0) {
            offer.setStatus(OfferStatus.THRESHOLD_REACHED);
        }

        offerRepository.save(offer);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "orderId", saved.getId(),
                "totalPrice", saved.getTotalAmount(),
                "status", saved.getStatus().name(),
                "message", "Commande enregistrée avec succès."
        ));
    }

    // ─── Consumer orders ─────────────────────────────────────────────────────

    @GetMapping("/api/v1/orders/mine")
    public ResponseEntity<?> myOrders(@AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        List<Order> orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(orders.stream().map(ConsumerOrderDto::from).collect(Collectors.toList()));
    }

    @PostMapping("/api/v1/orders/{id}/confirm-receipt")
    public ResponseEntity<?> confirmReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) return unauthorized();
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if (!order.getBuyer().getId().equals(user.getId())) return forbidden();

        if (order.getStatus() != OrderStatus.AT_RELAY && order.getStatus() != OrderStatus.DELIVERING) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cette commande ne peut pas être confirmée dans son état actuel."
            ));
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of("message", "Réception confirmée. Merci !"));
    }

    @PostMapping("/api/v1/orders/{id}/dispute")
    public ResponseEntity<?> dispute(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) return unauthorized();
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if (!order.getBuyer().getId().equals(user.getId())) return forbidden();

        order.setStatus(OrderStatus.DISPUTED);
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of(
                "message", "Litige ouvert. Notre équipe vous contactera sous 72h.",
                "reason", body.getOrDefault("reason", "")
        ));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Non authentifié."));
    }
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Accès refusé."));
    }
}
