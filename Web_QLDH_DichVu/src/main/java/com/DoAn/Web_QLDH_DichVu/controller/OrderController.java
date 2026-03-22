package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.entity.ServiceSetting;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.repository.ServiceSettingRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import com.DoAn.Web_QLDH_DichVu.service.BuffOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final ServiceSettingRepository settingRepo;
    private final BuffOrderService orderService;
    private final UserRepository userRepo;

    @GetMapping("/create")
    public String showCreateOrderForm(Model model, Principal principal) {
        // Truyền danh sách dịch vụ ra view
        List<ServiceSetting> services = settingRepo.findAll();
        model.addAttribute("services", services);

        // Truyền thông tin số dư của User ra view
        User user = userRepo.findByUsername(principal.getName()).get();
        model.addAttribute("currentUser", user);

        return "order/create";
    }

    @PostMapping("/create")
    public String processCreateOrder(
            @RequestParam Long serviceSettingId,
            @RequestParam String targetLink,
            @RequestParam int quantity,
            Principal principal,
            Model model) {
        try {
            // Lấy username từ Principal (người đang đăng nhập)
            String username = principal.getName();

            orderService.placeOrder(username, serviceSettingId, targetLink, quantity);
            model.addAttribute("successMessage", "Tạo đơn buff thành công! Hệ thống đang xử lý.");

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        // Load lại data hiển thị form
        model.addAttribute("services", settingRepo.findAll());
        model.addAttribute("currentUser", userRepo.findByUsername(principal.getName()).get());
        return "order/create";
    }
}