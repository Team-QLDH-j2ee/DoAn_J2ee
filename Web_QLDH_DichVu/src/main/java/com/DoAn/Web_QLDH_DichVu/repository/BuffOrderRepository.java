package com.DoAn.Web_QLDH_DichVu.repository;
import com.DoAn.Web_QLDH_DichVu.entity.*;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface BuffOrderRepository extends JpaRepository<BuffOrder, Long> {
    List<BuffOrder> findByUserOrderByCreatedAtDesc(User user);
    List<BuffOrder> findByStatus(OrderStatus status);
}
