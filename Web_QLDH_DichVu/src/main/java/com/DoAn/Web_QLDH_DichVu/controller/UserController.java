package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Xem thông tin cá nhân
    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", currentUser);

        return "customer/profile";
    }

    // 2. Hiển thị trang chỉnh sửa
    @GetMapping("/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", currentUser);

        return "customer/edit-profile";
    }

    // 3. Xử lý lưu thông tin chỉnh sửa (HÀM NÀY FILE CŨ CỦA EM BỊ THIẾU)
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User userDetails, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);

        if (user != null) {
            user.setFullName(userDetails.getFullName());
            user.setEmail(userDetails.getEmail());
            userRepository.save(user);

            ra.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        }

        return "redirect:/user/profile";
    }

    // 4. Trang nạp tiền (Chuẩn bị làm)
    @GetMapping("/recharge")
    public String viewRecharge(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", currentUser);
        return "customer/recharge";
    }
}