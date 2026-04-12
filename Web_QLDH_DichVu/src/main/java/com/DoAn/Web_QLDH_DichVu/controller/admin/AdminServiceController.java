package com.DoAn.Web_QLDH_DichVu.controller.admin;

import com.DoAn.Web_QLDH_DichVu.entity.ServiceSetting;
import com.DoAn.Web_QLDH_DichVu.repository.ServiceSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final ServiceSettingRepository settingRepo;

    // 1. Hiển thị trang quản lý (CÓ PHÂN TRANG)
    @GetMapping
    public String manageServices(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 5; // Cài 5 dịch vụ/trang để bảng không bị dài hơn form bên trái
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<ServiceSetting> pageData = settingRepo.findAll(pageable);

        model.addAttribute("services", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "admin/services"; // Trỏ tới file templates/admin/services.html
    }

    // 2. Xử lý thêm dịch vụ mới
    @PostMapping("/add")
    public String addService(@RequestParam String serviceName,
            @RequestParam String platform, // Cập nhật tham số platform
            @RequestParam BigDecimal basePrice,
            @RequestParam int defaultQuantity,
            RedirectAttributes redirectAttributes) {
        try {
            ServiceSetting newService = ServiceSetting.builder()
                    .serviceName(serviceName)
                    .platform(platform) // Lưu platform
                    .basePrice(basePrice)
                    .defaultQuantity(defaultQuantity)
                    .build();

            settingRepo.save(newService);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm dịch vụ [" + serviceName + "] thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm dịch vụ: " + e.getMessage());
        }

        return "redirect:/admin/services";
    }

    // 3. Xóa dịch vụ (Chức năng mở rộng cho Admin)
    @PostMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            settingRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa dịch vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa. Có thể dịch vụ này đang có đơn hàng ràng buộc.");
        }
        return "redirect:/admin/services";
    }

    // 4. Xử lý Cập nhật (Sửa) dịch vụ
    @PostMapping("/edit/{id}")
    public String editService(@PathVariable Long id,
            @RequestParam String serviceName,
            @RequestParam String platform,
            @RequestParam BigDecimal basePrice,
            @RequestParam int defaultQuantity,
            RedirectAttributes redirectAttributes) {
        try {
            // Tìm dịch vụ cũ trong DB
            ServiceSetting existingService = settingRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ với ID: " + id));

            // Cập nhật thông tin mới
            existingService.setServiceName(serviceName);
            existingService.setPlatform(platform);
            existingService.setBasePrice(basePrice);
            existingService.setDefaultQuantity(defaultQuantity);

            // Lưu lại vào DB
            settingRepo.save(existingService);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật dịch vụ [#" + id + "] thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }

        return "redirect:/admin/services";
    }
}