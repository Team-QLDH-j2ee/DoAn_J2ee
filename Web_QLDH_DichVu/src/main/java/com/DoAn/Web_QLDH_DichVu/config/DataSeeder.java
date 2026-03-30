package com.DoAn.Web_QLDH_DichVu.config;

import com.DoAn.Web_QLDH_DichVu.entity.BuffOrder;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import com.DoAn.Web_QLDH_DichVu.repository.BuffOrderRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDummyOrders(BuffOrderRepository orderRepo, UserRepository userRepo) {
        return args -> {
            // Lấy tài khoản khách hàng có username là "user" (Sếp có thể đổi thành "customer1" nếu muốn)
            User user = userRepo.findByUsername("user").orElse(null);

            // Kiểm tra xem user có tồn tại và hệ thống đang có ít đơn thì mới đẻ thêm
            if (user != null && orderRepo.count() < 100) {
                System.out.println("⏳ Đang tự động tạo 100 đơn hàng test...");

                for (int i = 1; i <= 100; i++) {
                    BuffOrder dummyOrder = BuffOrder.builder()
                            .user(user)
                            .targetLink("https://www.tiktok.com/@sondaika/video/" + i) // Sinh link ảo
                            .quantity(1000 + i)
                            .initialCount(100)
                            .currentCount(100)
                            .price(BigDecimal.valueOf(15000))
                            // Trộn lẫn các trạng thái cho nó đa dạng
                            .status(i % 3 == 0 ? OrderStatus.COMPLETED : (i % 5 == 0 ? OrderStatus.IN_PROGRESS : OrderStatus.PENDING))
                            // Cố tình lùi giờ lại (mỗi đơn cách nhau 1 phút) để test tính năng sắp xếp mới nhất lên đầu
                            .createdAt(LocalDateTime.now().minusMinutes(i))
                            .build();

                    orderRepo.save(dummyOrder);
                }

                System.out.println("✅ ĐÃ ĐẺ XONG 100 ĐƠN HÀNG! Sếp vào web kiểm tra phân trang nhé!");
            }
        };
    }
}