package com.DoAn.Web_QLDH_DichVu.repository;
import com.DoAn.Web_QLDH_DichVu.entity.*;
import com.DoAn.Web_QLDH_DichVu.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
