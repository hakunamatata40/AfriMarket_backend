package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.WalletDto;
import com.example.AfriMarket_backend.model.Transaction;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.model.enums.OrderStatus;
import com.example.AfriMarket_backend.repository.OrderRepository;
import com.example.AfriMarket_backend.repository.TransactionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/wallet")
public class ApiWalletController {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    public ApiWalletController(TransactionRepository transactionRepository,
                                OrderRepository orderRepository) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<?> getWallet(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // Available balance: sum of RELEASE transactions for this producer
        BigDecimal disponible = transactionRepository.sumReleasedForProducer(user.getId());
        if (disponible == null) disponible = BigDecimal.ZERO;

        // Pending: sum of paid orders not yet released
        BigDecimal enAttente = orderRepository.sumPendingEscrowForProducer(user.getId());
        if (enAttente == null) enAttente = BigDecimal.ZERO;

        // Recent transactions
        List<Transaction> txs = transactionRepository.findForProducer(user.getId(), 10, 0);
        List<WalletDto.TransactionDto> dtos = txs.stream()
                .map(WalletDto.TransactionDto::from).collect(Collectors.toList());

        return ResponseEntity.ok(new WalletDto(disponible, enAttente, dtos));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(@AuthenticationPrincipal User user,
                                              @RequestParam(defaultValue = "0") int page) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Transaction> txs = transactionRepository.findForProducer(user.getId(), 20, page * 20);
        return ResponseEntity.ok(txs.stream().map(WalletDto.TransactionDto::from).collect(Collectors.toList()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal User user,
                                       @RequestBody Map<String, Object> body) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // In production: trigger actual MoMo transfer. For now: return success simulation.
        Object amount = body.get("amount");
        return ResponseEntity.ok(Map.of(
                "message", "Demande de virement enregistrée. Traitement sous 24h.",
                "amount", amount != null ? amount : 0,
                "momoNumber", user.getMomoNumber() != null ? user.getMomoNumber() : ""
        ));
    }
}
