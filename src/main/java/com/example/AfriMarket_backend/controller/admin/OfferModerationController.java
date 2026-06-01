package com.example.AfriMarket_backend.controller.admin;

import com.example.AfriMarket_backend.service.OfferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/offers")
public class OfferModerationController {

    private static final Logger log = LoggerFactory.getLogger(OfferModerationController.class);
    private final OfferService offerService;

    public OfferModerationController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        try {
            log.info(">>> Loading /admin/offers page={}", page);
            var pageable = PageRequest.of(page, 15, Sort.by("createdAt").descending());
            var offers = offerService.findAll(pageable);
            log.info(">>> offers page loaded: {} items", offers.getNumberOfElements());
            var pending = offerService.getPendingOffers();
            log.info(">>> pending offers: {}", pending.size());
            model.addAttribute("offers", offers);
            model.addAttribute("pendingOffers", pending);
            model.addAttribute("page", "offers");
            log.info(">>> Rendering admin/offers/list");
            return "admin/offers/list";
        } catch (Exception e) {
            log.error("!!! ERREUR /admin/offers: {} — {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("offer", offerService.findById(id));
        model.addAttribute("page", "offers");
        return "admin/offers/detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        offerService.approve(id);
        ra.addFlashAttribute("success", "Offre approuvée et publiée.");
        return "redirect:/admin/offers";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason, RedirectAttributes ra) {
        offerService.reject(id, reason);
        ra.addFlashAttribute("success", "Offre rejetée. Le producteur a été notifié.");
        return "redirect:/admin/offers";
    }

    @PostMapping("/{id}/close")
    public String forceClose(@PathVariable Long id, RedirectAttributes ra) {
        offerService.forceClose(id);
        ra.addFlashAttribute("success", "Offre clôturée de force.");
        return "redirect:/admin/offers";
    }
}
