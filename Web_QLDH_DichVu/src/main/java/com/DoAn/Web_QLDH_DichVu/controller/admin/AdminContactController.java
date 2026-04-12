package com.DoAn.Web_QLDH_DichVu.controller.admin;

import com.DoAn.Web_QLDH_DichVu.entity.ContactMessage;
import com.DoAn.Web_QLDH_DichVu.entity.Notification;
import com.DoAn.Web_QLDH_DichVu.repository.ContactMessageRepository;
import com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository;
import com.DoAn.Web_QLDH_DichVu.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/contacts")
@RequiredArgsConstructor
public class AdminContactController {

    private final ContactMessageRepository contactRepo;
    private final NotificationRepository notificationRepo;
    private final EmailService emailService;


    @GetMapping
    public String listContacts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model,
            Principal principal) {

        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        int pageSize = 5;
        Pageable pageable = PageRequest.of(
                page - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ContactMessage> pageData = contactRepo.findAll(pageable);

        model.addAttribute("messages", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "admin/contacts";
    }

    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes ra) {
        contactRepo.deleteById(id);
        ra.addFlashAttribute("successMessage", "Đã xóa tin nhắn thành công!");
        return "redirect:/admin/contacts";
    }

    @PostMapping("/process/{id}")
    public String processMessage(@PathVariable Long id, RedirectAttributes ra) {
        contactRepo.findById(id).ifPresent(msg -> {
            msg.setProcessed(true);
            contactRepo.save(msg);

            if (msg.getEmail() != null && !msg.getEmail().isEmpty()) {
                emailService.sendContactConfirmationEmail(msg.getEmail(), msg.getName());
            }

            if (msg.getUser() != null) {
                notificationRepo.save(Notification.builder()
                        .user(msg.getUser())
                        .message("✅ Yêu cầu hỗ trợ lúc " +
                                String.format("%02d:%02d", msg.getCreatedAt().getHour(), msg.getCreatedAt().getMinute())
                                +
                                " đã được Admin tiếp nhận và đang xử lý!")
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        });
        ra.addFlashAttribute("successMessage", "Đã tiếp nhận và thông báo cho khách!");
        return "redirect:/admin/contacts";
    }
}