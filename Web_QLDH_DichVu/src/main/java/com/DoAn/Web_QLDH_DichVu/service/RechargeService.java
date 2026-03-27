package com.DoAn.Web_QLDH_DichVu.service;

import com.DoAn.Web_QLDH_DichVu.entity.Notification;
import com.DoAn.Web_QLDH_DichVu.entity.RechargeRequest;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.RequestStatus;
import com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository;
import com.DoAn.Web_QLDH_DichVu.repository.RechargeRequestRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RechargeService {

    private final RechargeRequestRepository rechargeRepo;
    private final UserRepository userRepo;
    private final NotificationRepository notificationRepo; // Tiêm kho thông báo vào

    // 1. DÀNH CHO KHÁCH HÀNG: Tạo yêu cầu nạp tiền
    @Transactional
    public RechargeRequest createRequest(String username, BigDecimal amount) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Số tiền nạp phải lớn hơn 0");

        RechargeRequest request = RechargeRequest.builder()
                .user(user).amount(amount).status(RequestStatus.PENDING).createdAt(LocalDateTime.now()).build();
        return rechargeRepo.save(request);
    }

    // 2. DÀNH CHO KHÁCH HÀNG: Lấy lịch sử nạp của mình
    public List<RechargeRequest> getUserRequests(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return rechargeRepo.findAll().stream()
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .sorted((r1, r2) -> r2.getId().compareTo(r1.getId())).toList();
    }

    // 3. DÀNH CHO ADMIN: Lấy tất cả yêu cầu nạp tiền hệ thống
    public List<RechargeRequest> getAllRequestsForAdmin() {
        return rechargeRepo.findAll().stream().sorted((r1, r2) -> r2.getId().compareTo(r1.getId())).toList();
    }

    // 4. DÀNH CHO ADMIN: Duyệt hoặc Từ chối nạp tiền (CÓ BẮN THÔNG BÁO)
    @Transactional(rollbackFor = Exception.class)
    public void processRechargeRequest(Long requestId, RequestStatus newStatus) {
        RechargeRequest request = rechargeRepo.findById(requestId).orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));
        if (request.getStatus() != RequestStatus.PENDING) throw new RuntimeException("Chỉ có thể xử lý yêu cầu đang chờ duyệt (PENDING)");

        String formattedAmount = String.format("%,d", request.getAmount().longValue()) + "đ";

        if (newStatus == RequestStatus.APPROVED) {
            User user = request.getUser();
            user.setBalance(user.getBalance().add(request.getAmount()));
            userRepo.save(user);

            // Tự động bắn thông báo CỘNG TIỀN
            notificationRepo.save(Notification.builder().user(user)
                    .message("💰 CHÚC MỪNG: Yêu cầu nạp " + formattedAmount + " của bạn đã được DUYỆT. Số dư đã được cộng thêm!")
                    .isRead(false).build());
        } else if (newStatus == RequestStatus.REJECTED) {
            // Tự động bắn thông báo TỪ CHỐI
            notificationRepo.save(Notification.builder().user(request.getUser())
                    .message("❌ RẤT TIẾC: Yêu cầu nạp " + formattedAmount + " của bạn đã bị TỪ CHỐI. Vui lòng liên hệ hỗ trợ.")
                    .isRead(false).build());
        }

        request.setStatus(newStatus);
        rechargeRepo.save(request);
    }
}