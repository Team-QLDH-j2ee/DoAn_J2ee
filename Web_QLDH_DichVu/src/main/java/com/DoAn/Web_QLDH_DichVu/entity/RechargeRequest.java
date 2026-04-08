package com.DoAn.Web_QLDH_DichVu.entity;

import com.DoAn.Web_QLDH_DichVu.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "recharge_requests")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RechargeRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(precision = 15, scale = 2, nullable = false) private BigDecimal amount;
    private String content;
    @Enumerated(EnumType.STRING) private RequestStatus status;
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}