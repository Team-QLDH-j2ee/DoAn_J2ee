package com.DoAn.Web_QLDH_DichVu.service;

import com.DoAn.Web_QLDH_DichVu.entity.BuffOrder;
import com.DoAn.Web_QLDH_DichVu.entity.Notification;
import com.DoAn.Web_QLDH_DichVu.entity.ServiceSetting;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import com.DoAn.Web_QLDH_DichVu.repository.BuffOrderRepository;
import com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository;
import com.DoAn.Web_QLDH_DichVu.repository.ServiceSettingRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuffOrderService {

    private final BuffOrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ServiceSettingRepository settingRepo;

    private final NotificationRepository notificationRepo;

    @Transactional(rollbackFor = Exception.class)
    public BuffOrder placeOrder(String username, Long serviceSettingId, String targetLink, int quantity) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

        ServiceSetting serviceSetting = settingRepo.findById(serviceSettingId)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại."));

        BigDecimal totalCost = serviceSetting.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        if (user.getBalance().compareTo(totalCost) < 0) {
            BigDecimal shortfall = totalCost.subtract(user.getBalance());
            throw new RuntimeException(
                    "Số dư không đủ! Tổng đơn: " +
                            String.format("%,.0f", totalCost) + " đ | Số dư hiện tại: " +
                            String.format("%,.0f", user.getBalance()) + " đ | Cần nạp thêm: " + String.format("%,.0f", shortfall) + " đ");
        }

        BuffOrder newOrder = BuffOrder.builder()
                .user(user)
                .targetLink(targetLink)
                .quantity(quantity)
                .price(totalCost)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return orderRepo.save(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, String username) {
        BuffOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thanh toán đơn hàng này.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán hoặc đã hủy.");
        }

        User user = order.getUser();

        if (user.getBalance().compareTo(order.getPrice()) < 0) {
            throw new RuntimeException("Số dư không đủ! Vui lòng nạp thêm tiền.");
        }

        user.setBalance(user.getBalance().subtract(order.getPrice()));
        userRepo.save(user);

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepo.save(order);

        log.info("Thanh toán thành công đơn #{} cho User: {}", orderId, username);
    }

    public Page<BuffOrder> getUserOrdersPaginated(String username, int pageNo, int pageSize) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        return orderRepo.findByUserOrderByCreatedAtDesc(user, pageable);
    }


    // ================== DÀNH CHO ADMIN ==================

    // Lấy tất cả đơn hàng của hệ thống (Sắp xếp mới nhất lên đầu)
    public java.util.List<BuffOrder> getAllOrdersForAdmin() {
        // Nếu sếp có custom query trong Repo thì xài, không thì dùng findAll() stream
        return orderRepo.findAll().stream()
                .sorted((o1, o2) -> {
                    // Sắp xếp theo ID giảm dần (đơn mới nhất lên đầu) nếu không có createdAt
                    return o2.getId().compareTo(o1.getId());
                })
                .toList();
    }

    // Admin cập nhật trạng thái đơn hàng (ĐÃ THÊM LOGIC BẮN THÔNG BÁO)
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus) {
        BuffOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        order.setStatus(newStatus);
        orderRepo.save(order);

        // Dịch trạng thái sang Tiếng Việt cho sinh động
        String statusVn = "";
        switch (newStatus.name()) {
            case "IN_PROGRESS":
                statusVn = "ĐANG CHẠY 🚀";
                break;
            case "COMPLETED":
                statusVn = "HOÀN THÀNH ✅";
                break;
            case "CANCELLED":
                statusVn = "ĐÃ HỦY ❌";
                break;
            default:
                statusVn = "CHỜ XỬ LÝ ⏳";
        }

        // Tự động bắn thông báo cho Khách hàng
        notificationRepo.save(Notification.builder()
                .user(order.getUser())
                .message("📦 Đơn hàng #" + order.getId() + " (" + order.getTargetLink()
                        + ") vừa được cập nhật sang trạng thái: " + statusVn)
                .isRead(false)
                .build());
    }
}