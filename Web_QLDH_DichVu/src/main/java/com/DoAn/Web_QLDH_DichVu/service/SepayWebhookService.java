package com.DoAn.Web_QLDH_DichVu.service;

import com.DoAn.Web_QLDH_DichVu.entity.Notification;
import com.DoAn.Web_QLDH_DichVu.entity.RechargeRequest;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.RequestStatus;
import com.DoAn.Web_QLDH_DichVu.repository.NotificationRepository;
import com.DoAn.Web_QLDH_DichVu.repository.RechargeRequestRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SepayWebhookService {

    private final UserRepository userRepository;
    private final RechargeRequestRepository rechargeRequestRepository;
    private final NotificationRepository notificationRepository;

    public SepayWebhookService(UserRepository userRepository,
                               RechargeRequestRepository rechargeRequestRepository,
                               NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.rechargeRequestRepository = rechargeRequestRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public String processWebhook(Map<String, Object> payload) {
        String transferContent = getString(payload, "content");
        if (transferContent == null || transferContent.isBlank()) {
            transferContent = getString(payload, "description");
        }

        String transactionId = getString(payload, "id");
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = getString(payload, "referenceCode");
        }

        if (transactionId != null && !transactionId.isBlank()
                && rechargeRequestRepository.existsByGatewayTransactionId(transactionId)) {
            return "Giao dịch đã xử lý trước đó";
        }

        BigDecimal amount = getBigDecimal(payload, "transferAmount");
        if (amount == null) {
            amount = getBigDecimal(payload, "amount");
        }

        String bankName = getString(payload, "gateway");
        String bankAccountNo = getString(payload, "accountNumber");

        String transferType = getString(payload, "transferType");
        if (transferType != null && !transferType.isBlank()
                && !transferType.equalsIgnoreCase("in")) {
            return "Bỏ qua giao dịch không phải tiền vào";
        }

        if (transferContent == null || transferContent.isBlank()) {
            return "Không có nội dung chuyển khoản";
        }

        String rechargeCode = extractRechargeCode(transferContent);
        if (rechargeCode == null) {
            return "Không tìm thấy mã nạp tiền trong nội dung chuyển khoản";
        }

        Optional<User> userOpt = userRepository.findByRechargeCode(rechargeCode);
        if (userOpt.isEmpty()) {
            return "Không tìm thấy user với mã nạp tiền: " + rechargeCode;
        }

        User user = userOpt.get();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Số tiền không hợp lệ";
        }

        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        RechargeRequest rechargeRequest = RechargeRequest.builder()
                .user(user)
                .amount(amount)
                .content(rechargeCode)
                .status(RequestStatus.APPROVED)
                .gatewayTransactionId(transactionId)
                .bankDescription(transferContent)
                .bankAccountNo(bankAccountNo)
                .bankName(bankName)
                .amountReceived(amount)
                .sepayRawData(String.valueOf(payload))
                .build();

        rechargeRequestRepository.save(rechargeRequest);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Bạn đã nạp thành công " + amount + " VNĐ vào tài khoản.");
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        return "Nạp tiền thành công cho user: " + user.getUsername();
    }

    private String extractRechargeCode(String content) {
        String normalized = content.toUpperCase().trim();

        Pattern pattern = Pattern.compile("NAP[A-Z0-9]+");
        Matcher matcher = pattern.matcher(normalized);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    private String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private BigDecimal getBigDecimal(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) return null;

        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }
}