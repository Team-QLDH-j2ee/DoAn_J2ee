package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.ContactMessage;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.repository.ContactMessageRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import com.DoAn.Web_QLDH_DichVu.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository contactRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    @GetMapping("/contact")
    public String showContactPage(Model model, Principal principal) {
        if (principal != null) {
            userRepo.findByUsername(principal.getName()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
            });
        }
        return "contact";
    }

    @PostMapping("/contact/send")
    public String submitContactForm(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String message,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy User đang đăng nhập (Nếu có)
            User user = (principal != null) ? userRepo.findByUsername(principal.getName()).orElse(null) : null;

            ContactMessage contactMsg = ContactMessage.builder()
                    .user(user) // Gắn User vào đây
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .isProcessed(false) // Mặc định là chưa xử lý
                    .build();

            contactRepo.save(contactMsg);
            
            // Gửi email xác nhận (Chạy ngầm @Async)
            emailService.sendContactConfirmationEmail(email, name);
            
            redirectAttributes.addFlashAttribute("successMessage", "Hệ thống đã ghi nhận liên hệ và sẽ phản hồi qua mail hoặc số điện thoại. Vui lòng kiểm tra hộp thư của bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra, vui lòng thử lại sau.");
        }
        return "redirect:/contact";
    }
}