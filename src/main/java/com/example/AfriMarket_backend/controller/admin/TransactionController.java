package com.example.AfriMarket_backend.controller.admin;

import com.example.AfriMarket_backend.service.TransactionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        model.addAttribute("transactions", transactionService.findAll(pageable));
        model.addAttribute("disputes", transactionService.getOpenDisputes());
        model.addAttribute("totalCommissions", transactionService.getTotalCommissions());
        model.addAttribute("page", "transactions");
        return "admin/transactions/list";
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
