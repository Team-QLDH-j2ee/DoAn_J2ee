package com.DoAn.Web_QLDH_DichVu.repository;

import com.DoAn.Web_QLDH_DichVu.entity.RechargeRequest;
import com.DoAn.Web_QLDH_DichVu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RechargeRequestRepository extends JpaRepository<RechargeRequest, Long> {
    List<RechargeRequest> findByUserOrderByCreatedAtDesc(User user);
    Optional<RechargeRequest> findByContent(String content);

    boolean existsByGatewayTransactionId(String gatewayTransactionId);
}