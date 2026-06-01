package com.example.AfriMarket_backend.controller.admin;

import com.example.AfriMarket_backend.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        try {
            log.info(">>> Loading /admin/transactions page={}", page);
            var pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
            var txs = transactionService.findAll(pageable);
            log.info(">>> transactions loaded: {}", txs.getNumberOfElements());
            var disputes = transactionService.getOpenDisputes();
            log.info(">>> disputes loaded: {}", disputes.size());
            var commissions = transactionService.getTotalCommissions();
            log.info(">>> commissions: {}", commissions);
            model.addAttribute("transactions", txs);
            model.addAttribute("disputes", disputes);
            model.addAttribute("totalCommissions", commissions);
            model.addAttribute("page", "transactions");
            log.info(">>> Rendering admin/transactions/list");
            return "admin/transactions/list";
        } catch (Exception e) {
            log.error("!!! ERREUR /admin/transactions: {} — {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/disputes/{id}/resolve")
    public String resolveDispute(@PathVariable Long id,
                                  @RequestParam String resolution,
                                  @RequestParam(defaultValue = "false") boolean refund,
                                  RedirectAttributes ra) {
        transactionService.resolveDispute(id, resolution, refund);
        ra.addFlashAttribute("success", "Litige résolu.");
        return "redirect:/admin/transactions";
    }
}
