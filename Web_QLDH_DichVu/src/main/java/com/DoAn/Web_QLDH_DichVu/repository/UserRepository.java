package com.DoAn.Web_QLDH_DichVu.repository;

import com.DoAn.Web_QLDH_DichVu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByRechargeCode(String rechargeCode);
    Optional<User> findByResetToken(String resetToken);
}