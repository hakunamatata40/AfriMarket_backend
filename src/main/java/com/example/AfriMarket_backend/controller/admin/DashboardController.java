package com.example.AfriMarket_backend.controller.admin;

import com.example.AfriMarket_backend.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAllAttributes(dashboardService.getStats());
        model.addAttribute("recentUsers", dashboardService.getRecentUsers());
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());
        model.addAttribute("page", "dashboard");
        return "admin/dashboard";
    }
}
