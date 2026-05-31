package com.example.AfriMarket_backend.controller.admin;

import com.example.AfriMarket_backend.service.SystemSettingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
public class SettingsController {

    private final SystemSettingService settingService;

    public SettingsController(SystemSettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public String settings(Model model) {
        model.addAttribute("settings", settingService.findAll());
        model.addAttribute("page", "settings");
        return "admin/settings/index";
    }

    @PostMapping
    public String save(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        // Remove Spring MVC internal params
        params.remove("_csrf");
        settingService.saveAll(params);
        ra.addFlashAttribute("success", "Paramètres sauvegardés avec succès.");
        return "redirect:/admin/settings";
    }
}
