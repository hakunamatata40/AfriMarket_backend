package com.example.AfriMarket_backend.controller.admin;

import com.example.AfriMarket_backend.model.enums.UserRole;
import com.example.AfriMarket_backend.model.enums.UserStatus;
import com.example.AfriMarket_backend.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String role,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        UserRole roleEnum = role != null && !role.isBlank() ? UserRole.valueOf(role) : null;
        UserStatus statusEnum = status != null && !status.isBlank() ? UserStatus.valueOf(status) : null;
        var pageable = PageRequest.of(page, 15, Sort.by("createdAt").descending());
        model.addAttribute("users", userService.findUsers(roleEnum, statusEnum, search, pageable));
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("page", "users");
        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("page", "users");
        return "admin/users/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        userService.updateStatus(id, UserStatus.valueOf(status));
        ra.addFlashAttribute("success", "Statut du compte mis à jour.");
        return "redirect:/admin/users/" + id;
    }
}
