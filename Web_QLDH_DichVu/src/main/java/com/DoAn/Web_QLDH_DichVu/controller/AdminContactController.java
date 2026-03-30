package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.ContactMessage;
import com.DoAn.Web_QLDH_DichVu.entity.Notification;
import com.DoAn.Web_QLDH_DichVu.repository.ContactMessageRepository;
import com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/contacts")
@RequiredArgsConstructor
public class AdminContactController {

    private final ContactMessageRepository contactRepo;
    private final NotificationRepository notificationRepo; // Bơm kho chuông vào

    // 1. Mở trang danh sách tin nhắn
    @GetMapping
    public String listContacts(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        // Lấy tất cả tin nhắn, xếp mới nhất lên đầu
        List<ContactMessage> messages = contactRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("messages", messages);
        return "admin/contacts";
    }

    // 2. Nút Xóa tin nhắn
    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes ra) {
        contactRepo.deleteById(id);
        ra.addFlashAttribute("successMessage", "Đã xóa tin nhắn thành công!");
        return "redirect:/admin/contacts";
    }

    // 3. NÚT TIẾP NHẬN & BẮN CHUÔNG CHO KHÁCH
    @PostMapping("/process/{id}")
    public String processMessage(@PathVariable Long id, RedirectAttributes ra) {
        contactRepo.findById(id).ifPresent(msg -> {
            msg.setProcessed(true); // Cập nhật trạng thái
            contactRepo.save(msg);

            // Nếu tin nhắn có dính với User (khách đã đăng nhập khi gửi) -> Bắn chuông
            if (msg.getUser() != null) {
                notificationRepo.save(Notification.builder()
                        .user(msg.getUser())
                        .message("✅ Yêu cầu hỗ trợ lúc " +
                                String.format("%02d:%02d", msg.getCreatedAt().getHour(), msg.getCreatedAt().getMinute()) +
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