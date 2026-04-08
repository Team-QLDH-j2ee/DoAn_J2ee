package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepo;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        if (principal != null) {

            String username = principal.getName();
            model.addAttribute("username", username);
            model.addAttribute("isLoggedIn", true);

            userRepository.findByUsername(username).ifPresent(user -> {

                // ✅ THÊM DÒNG NÀY
                model.addAttribute("currentUser", user);
                model.addAttribute("balance", user.getBalance());

                // Nếu là khách hàng → đếm thông báo
                if (user.getRole().name().equals("CUSTOMER")) {
                    long unreadCount = notificationRepo.findAll().stream()
                            .filter(n -> n.getUser().getId().equals(user.getId()) && !n.isRead())
                            .count();

                    model.addAttribute("unreadNotifCount", unreadCount);
                }
            });

        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}