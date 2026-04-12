package com.DoAn.Web_QLDH_DichVu.entity;

import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "buff_orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BuffOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, length = 500) private String targetLink;
    private int quantity;
    @Column(precision = 15, scale = 2) private BigDecimal price;
    @Enumerated(EnumType.STRING) private OrderStatus status;
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}