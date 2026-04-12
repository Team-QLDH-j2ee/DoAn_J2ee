package com.DoAn.Web_QLDH_DichVu.controller.admin;

import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import com.DoAn.Web_QLDH_DichVu.repository.BuffOrderRepository;
import com.DoAn.Web_QLDH_DichVu.service.BuffOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final BuffOrderService orderService;

    // SẾP THÊM ĐÚNG DÒNG NÀY VÀO LÀ HẾT BÁO ĐỎ NHÉ 👇
    private final BuffOrderRepository orderRepository;

    // 1. Hiển thị danh sách toàn bộ đơn hàng
    @GetMapping
    public String manageOrders(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 10; // 10 đơn 1 trang
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Lúc này orderRepository đã có mặt để phục vụ sếp!
        Page<com.DoAn.Web_QLDH_DichVu.entity.BuffOrder> pageData = orderRepository.findAll(pageable);

        model.addAttribute("orders", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "admin/orders";
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