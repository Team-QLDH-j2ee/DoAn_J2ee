package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import com.DoAn.Web_QLDH_DichVu.service.BuffOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final BuffOrderService orderService;

    // 1. Hiển thị danh sách toàn bộ đơn hàng
    @GetMapping
    public String manageOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrdersForAdmin());
        return "admin/orders"; // Trỏ tới file giao diện
    }

    // 2. Xử lý khi Admin đổi trạng thái đơn hàng
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus status,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatusByAdmin(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn #" + id + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}