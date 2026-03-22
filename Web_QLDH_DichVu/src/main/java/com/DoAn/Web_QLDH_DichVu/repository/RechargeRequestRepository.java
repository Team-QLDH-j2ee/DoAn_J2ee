package com.DoAn.Web_QLDH_DichVu.repository;
import com.DoAn.Web_QLDH_DichVu.entity.*;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface RechargeRequestRepository extends JpaRepository<RechargeRequest, Long> {
    List<RechargeRequest> findByUserOrderByCreatedAtDesc(User user);
    Optional<RechargeRequest> findByContent(String content);
}
