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

    // Hàm này sẽ chạy ngầm trước TẤT CẢ các trang để bơm dữ liệu cho Navbar
    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        if (principal != null) {
            // Đã đăng nhập: Bơm username và cờ isLoggedIn = true
            String username = principal.getName();
            model.addAttribute("username", username);
            model.addAttribute("isLoggedIn", true);

            userRepository.findByUsername(username).ifPresent(user -> {
                // Nếu là Khách hàng thì đếm thêm số thông báo chưa đọc
                if (user.getRole().name().equals("CUSTOMER")) {
                    long unreadCount = notificationRepo.findAll().stream()
                            .filter(n -> n.getUser().getId().equals(user.getId()) && !n.isRead())
                            .count();
                    model.addAttribute("unreadNotifCount", unreadCount);
                }
            });
        } else {
            // Chưa đăng nhập: Cờ isLoggedIn = false để hiện nút Đăng nhập/Đăng ký
            model.addAttribute("isLoggedIn", false);
        }
    }
}