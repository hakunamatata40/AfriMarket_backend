package com.example.AfriMarket_backend.service;

import com.example.AfriMarket_backend.model.Order;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.DisputeStatus;
import com.example.AfriMarket_backend.model.enums.OfferStatus;
import com.example.AfriMarket_backend.model.enums.OrderStatus;
import com.example.AfriMarket_backend.model.enums.UserStatus;
import com.example.AfriMarket_backend.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class DashboardService {

    private final OfferRepository offerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DisputeRepository disputeRepository;

    public DashboardService(OfferRepository offerRepository, OrderRepository orderRepository,
                            UserRepository userRepository, DisputeRepository disputeRepository) {
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.disputeRepository = disputeRepository;
    }

    public long countActiveOffers() {
        return offerRepository.countByStatus(OfferStatus.ACTIVE)
             + offerRepository.countByStatus(OfferStatus.THRESHOLD_REACHED);
    }

    public long countPendingOffers() {
        return offerRepository.countByStatus(OfferStatus.PENDING_REVIEW);
    }

    public long countOngoingOrders() {
        return orderRepository.countByStatus(OrderStatus.PAID)
             + orderRepository.countByStatus(OrderStatus.CONFIRMED)
             + orderRepository.countByStatus(OrderStatus.DELIVERING)
             + orderRepository.countByStatus(OrderStatus.AT_RELAY);
    }

    public BigDecimal getTotalEscrowValue() {
        return orderRepository.sumAmountByStatus(OrderStatus.PAID);
    }

    public BigDecimal getTodayRevenue() {
        try {
            return orderRepository.sumTodayRevenue();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public long countOpenDisputes() {
        return disputeRepository.countByStatus(DisputeStatus.OPEN);
    }

    public long countPendingUsers() {
        return userRepository.countByStatus(UserStatus.PENDING);
    }

    public List<User> getRecentUsers() {
        return userRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeOffers", countActiveOffers());
        stats.put("ongoingOrders", countOngoingOrders());
        stats.put("escrowValue", getTotalEscrowValue());
        stats.put("todayRevenue", getTodayRevenue());
        stats.put("openDisputes", countOpenDisputes());
        stats.put("pendingUsers", countPendingUsers());
        stats.put("pendingOffers", countPendingOffers());
        return stats;
    }
}
