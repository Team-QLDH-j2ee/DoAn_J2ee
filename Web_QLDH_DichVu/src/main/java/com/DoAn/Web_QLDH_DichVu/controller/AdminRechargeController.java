package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.enums.RequestStatus;
import com.DoAn.Web_QLDH_DichVu.service.RechargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/recharge")
@RequiredArgsConstructor
public class AdminRechargeController {

    private final RechargeService rechargeService;

    // Xem danh sách yêu cầu
    @GetMapping
    public String manageRecharge(Model model) {
        model.addAttribute("requests", rechargeService.getAllRequestsForAdmin());
        return "admin/recharge";
    }

    // Nút Duyệt / Từ chối
    @PostMapping("/process/{id}")
    public String processRequest(@PathVariable Long id, @RequestParam RequestStatus status, RedirectAttributes redirectAttributes) {
        try {
            rechargeService.processRechargeRequest(id, status);
            String action = status == RequestStatus.APPROVED ? "DUYỆT và CỘNG TIỀN" : "TỪ CHỐI";
            redirectAttributes.addFlashAttribute("successMessage", "Đã " + action + " phiếu nạp #" + id + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/recharge";
    }
}