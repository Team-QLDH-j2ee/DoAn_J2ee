package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import com.DoAn.Web_QLDH_DichVu.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // KHO THÔNG BÁO BẮT BUỘC PHẢI CÓ
    @Autowired
    private com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository notificationRepo;

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

    // 3. Xử lý lưu thông tin chỉnh sửa
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

    // 3.1. Hiển thị form đổi mật khẩu
    @GetMapping("/change-password")
    public String viewChangePassword(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", currentUser);

        return "customer/change-password";
    }

    // 3.2. Xử lý đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu cũ không chính xác!");
            return "redirect:/user/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        ra.addFlashAttribute("success", "Đổi mật khẩu thành công! Bạn có thể sử dụng mật khẩu mới ở lần đăng nhập sau.");
        return "redirect:/user/profile";
    }

    // 4. Trang nạp tiền
    @GetMapping("/recharge")
    public String viewRecharge(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);

        // ========================
        // 🔥 TẠO QR VIETQR
        // ========================
        String bank = "MB"; // đổi nếu cần
        String account = "0389306604"; // STK của bạn

        String content = currentUser.getRechargeCode();

        String qrUrl = "https://img.vietqr.io/image/"
                + bank + "-" + account + "-qr_only.png?addInfo=" + content;

        // ========================
        // ĐẨY RA VIEW
        // ========================
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("qrUrl", qrUrl);

        model.addAttribute("rechargeHistory", rechargeService.getUserRequests(principal.getName()));

        return "customer/recharge";
    }

    // 5. Xử lý Tạo Phiếu Nạp
    @PostMapping("/recharge/create")
    public String createRechargeRequest(@RequestParam BigDecimal amount, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            rechargeService.createRequest(principal.getName(), amount);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo yêu cầu nạp " + String.format("%,d", amount.longValue()) + " VNĐ thành công! Vui lòng chuyển khoản theo hướng dẫn bên dưới.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/user/recharge";
    }

    // =========================================
    // KHU VỰC THÔNG BÁO (NOTIFICATION)
    // =========================================

    // 6. Mở trang Danh sách Thông Báo (CHỨC NĂNG MỚI)
    @GetMapping("/notifications")
    public String viewNotifications(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            // Lấy tất cả thông báo của user, xếp mới nhất lên đầu
            java.util.List<com.DoAn.Web_QLDH_DichVu.entity.Notification> allNotifs = notificationRepo.findAll().stream()
                    .filter(n -> n.getUser().getId().equals(user.getId()))
                    .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                    .toList();

            model.addAttribute("notifications", allNotifs);
            model.addAttribute("user", user); // Đẩy ra để làm header
            model.addAttribute("username", user.getUsername()); // Cho Navbar index
        });

        return "customer/notifications";
    }

    // 7. Xử lý đánh dấu đã đọc
    @PostMapping("/notifications/mark-read")
    public String markNotificationsAsRead(Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                // Chỉ lấy những tin chưa đọc (isRead = false) để update
                java.util.List<com.DoAn.Web_QLDH_DichVu.entity.Notification> unreadNotifs = notificationRepo.findAll().stream()
                        .filter(n -> n.getUser().getId().equals(user.getId()) && !n.isRead())
                        .toList();

                unreadNotifs.forEach(n -> n.setRead(true));
                notificationRepo.saveAll(unreadNotifs);
            });
        }
        // Redirect về lại trang danh sách thông báo để user thấy viền xanh biến mất
        return "redirect:/user/notifications";
    }
}