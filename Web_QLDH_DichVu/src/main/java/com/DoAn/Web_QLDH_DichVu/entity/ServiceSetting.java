package com.DoAn.Web_QLDH_DichVu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "service_settings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceSetting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String serviceName;
    @Column(precision = 15, scale = 2, nullable = false) private BigDecimal basePrice;
    private int defaultQuantity;
}