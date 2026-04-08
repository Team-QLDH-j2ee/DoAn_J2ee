package com.DoAn.Web_QLDH_DichVu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nối với User để biết đường bắn thông báo
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;

    // Trạng thái: Admin đã bấm tiếp nhận chưa?
    private boolean isProcessed;
}