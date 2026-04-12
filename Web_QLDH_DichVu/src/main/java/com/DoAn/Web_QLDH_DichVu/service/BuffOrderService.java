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

    // ĐÃ THÊM KHO THÔNG BÁO VÀO ĐÂY
    private final NotificationRepository notificationRepo;

    // 1. TẠO ĐƠN (KHÔNG TRỪ TIỀN)
    @Transactional(rollbackFor = Exception.class)
    public BuffOrder placeOrder(String username, Long serviceSettingId, String targetLink, int quantity) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

        ServiceSetting serviceSetting = settingRepo.findById(serviceSettingId)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại."));

        BigDecimal totalCost = serviceSetting.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        // Kiểm tra số dư trước khi tạo đơn
        if (user.getBalance().compareTo(totalCost) < 0) {
            BigDecimal shortfall = totalCost.subtract(user.getBalance());
            throw new RuntimeException(
                    "Số dư không đủ! Tổng đơn: " +
                            String.format("%,.0f", totalCost) + " đ | Số dư hiện tại: " +
                            String.format("%,.0f", user.getBalance()) + " đ | Cần nạp thêm: " +
                            String.format("%,.0f", shortfall) + " đ");
        }

        // Tạo đơn với trạng thái PENDING (Chờ thanh toán), chưa trừ tiền User
        BuffOrder newOrder = BuffOrder.builder()
                .user(user)
                .targetLink(targetLink)
                .quantity(quantity)
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

    // 3. LẤY DANH SÁCH ĐƠN CỦA USER (ĐÃ SỬA THÀNH PHÂN TRANG)
    public Page<BuffOrder> getUserOrdersPaginated(String username, int pageNo, int pageSize) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

        // Pageable đếm từ 0, nên trang số 1 ở view truyền xuống phải -1
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        return orderRepo.findByUserOrderByCreatedAtDesc(user, pageable);
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