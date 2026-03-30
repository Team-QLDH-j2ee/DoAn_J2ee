package com.DoAn.Web_QLDH_DichVu.repository;

import com.DoAn.Web_QLDH_DichVu.entity.*;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BuffOrderRepository extends JpaRepository<BuffOrder, Long> {

    // Đã sửa lại để hỗ trợ phân trang Pageable
    Page<BuffOrder> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<BuffOrder> findByStatus(OrderStatus status);
}