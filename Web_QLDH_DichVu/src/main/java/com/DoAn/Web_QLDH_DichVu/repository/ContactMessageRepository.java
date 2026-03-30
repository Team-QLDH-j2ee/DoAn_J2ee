package com.DoAn.Web_QLDH_DichVu.repository;

import com.DoAn.Web_QLDH_DichVu.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}