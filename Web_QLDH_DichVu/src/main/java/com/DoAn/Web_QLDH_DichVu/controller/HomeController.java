package com.DoAn.Web_QLDH_DichVu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (principal != null) {
            // Đã đăng nhập
            model.addAttribute("welcomeMessage",
                    "Xin chào " + principal.getName() + ", mừng bạn quay lại hệ thống SMM Panel!");
            model.addAttribute("isLoggedIn", true); // Cờ báo hiệu cho giao diện
        } else {
            // Chưa đăng nhập
            model.addAttribute("welcomeMessage", "Chào mừng các thành viên Team QLDH đến với dự án J2EE SMM Panel!");
            model.addAttribute("isLoggedIn", false);
        }

        return "index"; // Trả về file index.html
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}