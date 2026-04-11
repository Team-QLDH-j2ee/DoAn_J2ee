package com.DoAn.Web_QLDH_DichVu.entity;

import com.DoAn.Web_QLDH_DichVu.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recharge_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    private String content;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;

    // mã giao dịch duy nhất từ phía cổng thanh toán / webhook
    @Column(name = "gateway_transaction_id", unique = true, length = 100)
    private String gatewayTransactionId;

    // nội dung chuyển khoản thực tế từ ngân hàng
    @Column(name = "bank_description", columnDefinition = "TEXT")
    private String bankDescription;

    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "amount_received", precision = 15, scale = 2)
    private BigDecimal amountReceived;

    // lưu toàn bộ payload webhook để debug khi cần
    @Column(name = "sepay_raw_data", columnDefinition = "TEXT")
    private String sepayRawData;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}