package com.DoAn.Web_QLDH_DichVu.repository;

import com.DoAn.Web_QLDH_DichVu.entity.ServiceSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceSettingRepository extends JpaRepository<ServiceSetting, Long> {
    // Hàm mới: Lọc danh sách dịch vụ theo tên nền tảng (INSTAGRAM, FACEBOOK, TIKTOK)
    List<ServiceSetting> findByPlatform(String platform);
}