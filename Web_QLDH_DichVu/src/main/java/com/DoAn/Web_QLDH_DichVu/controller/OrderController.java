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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final ServiceSettingRepository settingRepo;
    private final BuffOrderService orderService;
    private final UserRepository userRepo;

    @GetMapping("/create")
    public String showCreateOrderForm(
            @RequestParam(required = false, defaultValue = "INSTAGRAM") String platform,
            Model model, Principal principal) {

        if (principal == null) return "redirect:/login";

        // Lấy toàn bộ dịch vụ truyền ra View để JS tự lọc
        List<ServiceSetting> services = settingRepo.findAll();
        model.addAttribute("services", services);

        // Truyền nền tảng mặc định (hoặc từ trang chủ click vào)
        model.addAttribute("currentPlatform", platform.toUpperCase());

        userRepo.findByUsername(principal.getName()).ifPresent(user -> model.addAttribute("currentUser", user));
        return "order/create";
    }

    @PostMapping("/create")
    public String processCreateOrder(
            @RequestParam Long serviceSettingId,
            @RequestParam String targetLink,
            @RequestParam int quantity,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            orderService.placeOrder(principal.getName(), serviceSettingId, targetLink, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo đơn thành công! Vui lòng vào lịch sử để thanh toán.");
            // Đặt xong thì chuyển thẳng sang trang Lịch sử
            return "redirect:/order/history";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/order/create";
        }
    }

    // MỚI: Trang Lịch sử đơn hàng
    @GetMapping("/history")
    public String showOrderHistory(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        userRepo.findByUsername(principal.getName()).ifPresent(user -> model.addAttribute("currentUser", user));
        model.addAttribute("orders", orderService.getUserOrders(principal.getName()));

        return "order/history";
    }

    // MỚI: Xử lý nút thanh toán đơn hàng
    @PostMapping("/pay/{id}")
    public String payOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            orderService.payOrder(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công! Hệ thống bắt đầu chạy dịch vụ.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/order/history";
    }

    // MỚI: Xử lý Hủy đơn hàng
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        try {
            orderService.cancelOrder(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id + " thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/order/history";
    }

    // MỚI: Xử lý Sửa đơn hàng
    @PostMapping("/edit/{id}")
    public String editOrder(@PathVariable Long id,
                            @RequestParam String targetLink,
                            @RequestParam int quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        try {
            orderService.updatePendingOrder(id, principal.getName(), targetLink, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật đơn hàng #" + id + " thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/order/history";
    }
}