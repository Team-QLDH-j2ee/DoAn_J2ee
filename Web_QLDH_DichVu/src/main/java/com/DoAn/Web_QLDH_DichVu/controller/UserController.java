package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/user") // Tất cả các đường dẫn sẽ bắt đầu bằng /user
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Xem thông tin cá nhân: /user/profile
    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", currentUser);

        // Trỏ đúng vào thư mục templates/customer/profile.html
        return "customer/profile";
    }

    // 2. Trang nạp tiền (Chuẩn bị làm): /user/recharge
    @GetMapping("/recharge")
    public String viewRecharge(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", currentUser);
        return "customer/recharge";
    }

    // 3. Cập nhật thông tin (Dự phòng): /user/edit
    @GetMapping("/edit")
    public String editProfile() {
        return "customer/edit-profile";
    }
}