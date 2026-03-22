package com.DoAn.Web_QLDH_DichVu.service;

import com.DoAn.Web_QLDH_DichVu.entity.BuffOrder;
import com.DoAn.Web_QLDH_DichVu.entity.ServiceSetting;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import com.DoAn.Web_QLDH_DichVu.repository.BuffOrderRepository;
import com.DoAn.Web_QLDH_DichVu.repository.ServiceSettingRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BuffOrderService {

    private final BuffOrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ServiceSettingRepository settingRepo;
    private final ScraperService scraperService;

    @Transactional
    public BuffOrder placeOrder(String username, Long serviceSettingId, String targetLink, int quantity) {

        // 1. Tìm User đang đăng nhập
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực người dùng."));

        // 2. Tìm Dịch vụ khách chọn
        ServiceSetting serviceSetting = settingRepo.findById(serviceSettingId)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại."));

        // 3. Tính tổng tiền
        BigDecimal totalCost = serviceSetting.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        // 4. Kiểm tra số dư
        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Số dư không đủ! Cần: " + totalCost + " VNĐ, Hiện có: " + user.getBalance() + " VNĐ");
        }

        // 5. Cào Jsoup lấy số lượng ban đầu
        int initialCount = scraperService.getInitialCount(targetLink);

        // 6. Trừ tiền User
        user.setBalance(user.getBalance().subtract(totalCost));
        userRepo.save(user);

        // 7. Tạo và lưu đơn hàng
        BuffOrder newOrder = BuffOrder.builder()
                .user(user)
                .targetLink(targetLink)
                .quantity(quantity)
                .initialCount(initialCount)
                .currentCount(initialCount)
                .price(totalCost)
                .status(OrderStatus.IN_PROGRESS)
                .build();

        return orderRepo.save(newOrder);
    }
}