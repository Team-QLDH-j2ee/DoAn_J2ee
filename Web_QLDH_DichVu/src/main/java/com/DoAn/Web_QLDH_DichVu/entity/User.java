package com.DoAn.Web_QLDH_DichVu.entity;

import com.DoAn.Web_QLDH_DichVu.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean isLocked = false;

    @Column(name = "recharge_code", unique = true, length = 50)
    private String rechargeCode;

    @Column(name = "reset_token", length = 100)
    private String resetToken;
}