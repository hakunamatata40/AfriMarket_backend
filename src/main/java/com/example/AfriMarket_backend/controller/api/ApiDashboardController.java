package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.DashboardStatsDto;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.OfferStatus;
import com.example.AfriMarket_backend.repository.OfferRepository;
import com.example.AfriMarket_backend.repository.OrderRepository;
import com.example.AfriMarket_backend.repository.TransactionRepository;
import com.example.AfriMarket_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/dashboard")
public class ApiDashboardController {

    private final OfferRepository offerRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ApiDashboardController(OfferRepository offerRepository, OrderRepository orderRepository,
                                   TransactionRepository transactionRepository, UserRepository userRepository) {
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        long activeOffers = offerRepository.countByProducerIdAndStatus(user.getId(), OfferStatus.ACTIVE)
                          + offerRepository.countByProducerIdAndStatus(user.getId(), OfferStatus.THRESHOLD_REACHED);
        long pendingOffers = offerRepository.countByProducerIdAndStatus(user.getId(), OfferStatus.PENDING_REVIEW);
        long completedOffers = offerRepository.countByProducerIdAndStatus(user.getId(), OfferStatus.COMPLETED);

        BigDecimal monthRevenue = transactionRepository.sumMonthRevenueForProducer(user.getId());
        if (monthRevenue == null) monthRevenue = BigDecimal.ZERO;

        BigDecimal pendingEscrow = orderRepository.sumPendingEscrowForProducer(user.getId());
        if (pendingEscrow == null) pendingEscrow = BigDecimal.ZERO;

        User fresh = userRepository.findById(user.getId()).orElse(user);

        return ResponseEntity.ok(new DashboardStatsDto(
                activeOffers, pendingOffers, completedOffers,
                monthRevenue, pendingEscrow,
                fresh.getRatingAvg(), fresh.getRatingCount()
        ));
    }
}
