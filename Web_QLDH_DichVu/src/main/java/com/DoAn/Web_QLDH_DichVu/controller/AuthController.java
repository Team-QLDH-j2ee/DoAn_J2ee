package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.Role;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Hiển thị trang Đăng nhập
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // Trỏ tới file login.html
    }

    // 2. Hiển thị trang Đăng ký
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register"; // Trỏ tới file register.html
    }

    // 3. Xử lý logic Đăng ký
    @PostMapping("/register")
    public String processRegister(User user, Model model) {

        // Kiểm tra trùng Username
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "auth/register";
        }

        // Kiểm tra trùng Email
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "auth/register";
        }

        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set role
        user.setRole(Role.CUSTOMER);

        // 🔥 THÊM DÒNG NÀY
        user.setRechargeCode("NAP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Lưu DB
        userRepository.save(user);

        return "redirect:/login?success";
    }
}