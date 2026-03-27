package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.Notification;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.Role;
import com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Cần cái này để băm mật khẩu khi tạo/sửa user

    // ĐÃ THÊM KHO THÔNG BÁO VÀO ĐÂY
    private final NotificationRepository notificationRepo;

    // 1. Hiển thị danh sách thành viên
    @GetMapping
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    // 2. Thêm mới người dùng
    @PostMapping("/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email,
                             @RequestParam Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại trong hệ thống!");
            }

            User newUser = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password)) // Mã hóa mật khẩu
                    .email(email)
                    .role(role)
                    .balance(BigDecimal.ZERO)
                    .isLocked(false)
                    .build();

            userRepository.save(newUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản [" + username + "] thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 3. Sửa thông tin & Set Role
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String email,
                             @RequestParam Role role,
                             @RequestParam(required = false) String password,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

            user.setEmail(email);
            user.setRole(role); // Set lại Role theo Admin chọn

            // Nếu Admin có nhập mật khẩu mới thì mới đổi, không thì giữ nguyên mật khẩu cũ
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản [" + user.getUsername() + "] thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 4. Xóa người dùng (Bảo vệ Admin)
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

            // LOGIC BẢO VỆ ADMIN: Tránh bị bay màu
            if (user.getRole().name().equals("ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài khoản mang quyền Quản trị viên (ADMIN)!");
                return "redirect:/admin/users";
            }

            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa vĩnh viễn tài khoản [" + user.getUsername() + "].");
        } catch (Exception e) {
            // Nếu user đã có lịch sử nạp tiền hoặc đơn hàng, Database sẽ báo lỗi khóa ngoại (Foreign Key) không cho xóa
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa! Người dùng này đang chứa dữ liệu Đơn hàng hoặc Lịch sử nạp tiền trong hệ thống.");
        }
        return "redirect:/admin/users";
    }

    // 5. Xử lý Khóa / Mở khóa tài khoản (ĐÃ THÊM LOGIC BẮN THÔNG BÁO KHI MỞ KHÓA)
    @PostMapping("/toggle-lock/{id}")
    public String toggleLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
            if (user.getRole().name().equals("ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể khóa tài khoản của Quản trị viên!");
                return "redirect:/admin/users";
            }
            user.setLocked(!user.isLocked());
            userRepository.save(user);

            // Tự động bắn thông báo khi MỞ KHÓA
            if (!user.isLocked()) {
                notificationRepo.save(Notification.builder()
                        .user(user)
                        .message("🔓 Báo hỷ: Tài khoản của bạn đã được Admin MỞ KHÓA. Chào mừng quay trở lại hệ thống SMM Panel!")
                        .isRead(false)
                        .build());
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã " + (user.isLocked() ? "Khóa" : "Mở khóa") + " tài khoản [" + user.getUsername() + "] thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}