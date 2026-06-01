package com.example.AfriMarket_backend.controller.admin;

import com.example.AfriMarket_backend.model.RelayPoint;
import com.example.AfriMarket_backend.model.enums.RelayStatus;
import com.example.AfriMarket_backend.service.RelayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/relays")
public class RelayManagementController {

    private static final Logger log = LoggerFactory.getLogger(RelayManagementController.class);
    private final RelayService relayService;

    public RelayManagementController(RelayService relayService) {
        this.relayService = relayService;
    }

    @GetMapping
    public String list(Model model) {
        try {
            log.info(">>> Loading /admin/relays ...");
            var relays = relayService.findAll();
            log.info(">>> relays loaded: {}", relays.size());
            var zones = relayService.findAllZones();
            log.info(">>> zones loaded: {}", zones.size());
            model.addAttribute("relays", relays);
            model.addAttribute("zones", zones);
            model.addAttribute("newRelay", new RelayPoint());
            model.addAttribute("page", "relays");
            log.info(">>> Rendering template admin/relays/list");
            return "admin/relays/list";
        } catch (Exception e) {
            log.error("!!! ERREUR /admin/relays: {} — {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e; // re-throw so Spring shows the error
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("relay", relayService.findById(id));
        model.addAttribute("zones", relayService.findAllZones());
        model.addAttribute("page", "relays");
        return "admin/relays/edit";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute RelayPoint relay,
                       @RequestParam(required = false) Long zoneId,
                       RedirectAttributes ra) {
        if (zoneId != null) {
            relayService.findAllZones().stream()
                    .filter(z -> z.getId().equals(zoneId))
                    .findFirst()
                    .ifPresent(relay::setZone);
        }
        relayService.save(relay);
        ra.addFlashAttribute("success", "Point relais enregistré.");
        return "redirect:/admin/relays";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes ra) {
        relayService.updateStatus(id, RelayStatus.valueOf(status));
        ra.addFlashAttribute("success", "Statut du relais mis à jour.");
        return "redirect:/admin/relays";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        relayService.delete(id);
        ra.addFlashAttribute("success", "Point relais supprimé.");
        return "redirect:/admin/relays";
    }
}
