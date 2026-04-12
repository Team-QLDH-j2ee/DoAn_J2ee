package com.DoAn.Web_QLDH_DichVu.controller.admin;

import com.DoAn.Web_QLDH_DichVu.enums.RequestStatus;
import com.DoAn.Web_QLDH_DichVu.repository.RechargeRequestRepository; // IMPORT KHO CHỨA
import com.DoAn.Web_QLDH_DichVu.service.RechargeService;
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
@RequestMapping("/admin/recharge")
@RequiredArgsConstructor
public class AdminRechargeController {

    private final RechargeService rechargeService;

    // ĐỪNG QUÊN THÊM DÒNG NÀY ĐỂ KHÔNG BỊ BÁO ĐỎ:
    private final RechargeRequestRepository rechargeRepo;

    // Xem danh sách yêu cầu (ĐÃ ỐP PHÂN TRANG)
    @GetMapping
    public String manageRecharge(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 5; // 10 phiếu nạp 1 trang
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Lấy data từ Kho chứa (Repo) thay vì Service
        Page<com.DoAn.Web_QLDH_DichVu.entity.RechargeRequest> pageData = rechargeRepo.findAll(pageable);

        model.addAttribute("requests", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "admin/recharge";
    }

    // Nút Duyệt / Từ chối
    @PostMapping("/process/{id}")
    public String processRequest(@PathVariable Long id, @RequestParam RequestStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            rechargeService.processRechargeRequest(id, status);
            String action = status == RequestStatus.APPROVED ? "DUYỆT và CỘNG TIỀN" : "TỪ CHỐI";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã " + action + " phiếu nạp #" + id + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/recharge";
    }
}