package com.DoAn.Web_QLDH_DichVu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Gửi một câu chào từ Backend sang Frontend
        model.addAttribute("welcomeMessage", "Chào mừng các thành viên Team QLDH đến với dự án J2EE SMM Panel!");
        return "index"; // Trỏ tới file index.html trong thư mục templates
    }
}