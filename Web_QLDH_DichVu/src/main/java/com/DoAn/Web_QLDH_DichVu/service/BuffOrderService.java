package com.DoAn.Web_QLDH_DichVu.service;

import com.DoAn.Web_QLDH_DichVu.entity.BuffOrder;
import com.DoAn.Web_QLDH_DichVu.entity.ServiceSetting;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import com.DoAn.Web_QLDH_DichVu.repository.BuffOrderRepository;
import com.DoAn.Web_QLDH_DichVu.repository.ServiceSettingRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ScraperService scraperService;

    // 1. TẠO ĐƠN (KHÔNG TRỪ TIỀN)
    @Transactional(rollbackFor = Exception.class)
    public BuffOrder placeOrder(String username, Long serviceSettingId, String targetLink, int quantity) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

        ServiceSetting serviceSetting = settingRepo.findById(serviceSettingId)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại."));

        BigDecimal totalCost = serviceSetting.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        log.info("Đang lấy initialCount cho link: {}", targetLink);
        int initialCount = scraperService.getInitialCount(targetLink);

        // Tạo đơn với trạng thái PENDING (Chờ thanh toán), chưa trừ tiền User
        BuffOrder newOrder = BuffOrder.builder()
                .user(user)
                .targetLink(targetLink)
                .quantity(quantity)
                .initialCount(initialCount)
                .currentCount(initialCount)
                .price(totalCost)
                .status(OrderStatus.PENDING) // Quan trọng: Đổi thành PENDING
                .createdAt(LocalDateTime.now())
                .build();

        return orderRepo.save(newOrder);
    }

    // 2. THANH TOÁN ĐƠN HÀNG ĐÃ TẠO
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, String username) {
        // Tìm đơn hàng và kiểm tra xem có phải của user đang đăng nhập không
        BuffOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thanh toán đơn hàng này.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán hoặc đã hủy.");
        }

        User user = order.getUser();

        // Kiểm tra số dư
        if (user.getBalance().compareTo(order.getPrice()) < 0) {
            throw new RuntimeException("Số dư không đủ! Vui lòng nạp thêm tiền.");
        }

        // Trừ tiền và cập nhật trạng thái đơn
        user.setBalance(user.getBalance().subtract(order.getPrice()));
        userRepo.save(user);

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepo.save(order);

        log.info("Thanh toán thành công đơn #{} cho User: {}", orderId, username);
    }

    // 3. LẤY DANH SÁCH ĐƠN CỦA USER
    public java.util.List<BuffOrder> getUserOrders(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
        // Cần thêm hàm findByUserOrderByCreatedAtDesc trong BuffOrderRepository
        // Tạm thời dùng findAll() lọc bằng stream hoặc sếp viết thêm query trong Repo nhé
        return orderRepo.findAll().stream()
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .toList();
    }

    // --- CÁC HÀM MỚI BỔ SUNG CHO TÍNH NĂNG SỬA/HỦY ---

    // 4. HỦY ĐƠN HÀNG (Chỉ áp dụng cho PENDING)
    @Transactional
    public void cancelOrder(Long orderId, String username) {
        BuffOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền thao tác trên đơn hàng này.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ thanh toán.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
    }

    // 5. CẬP NHẬT ĐƠN HÀNG (Chỉ áp dụng cho PENDING)
    @Transactional
    public void updatePendingOrder(Long orderId, String username, String newLink, int newQuantity) {
        BuffOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền thao tác trên đơn hàng này.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể sửa đơn hàng đang chờ thanh toán.");
        }

        // Tính lại đơn giá gốc (Unit Price) từ dữ liệu cũ
        BigDecimal unitPrice = order.getPrice().divide(BigDecimal.valueOf(order.getQuantity()));

        // Cập nhật thông tin mới
        order.setTargetLink(newLink);
        order.setQuantity(newQuantity);
        order.setPrice(unitPrice.multiply(BigDecimal.valueOf(newQuantity)));

        orderRepo.save(order);
    }
}