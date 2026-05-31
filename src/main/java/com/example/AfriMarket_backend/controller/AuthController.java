package com.example.AfriMarket_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String root() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";
    }
}
