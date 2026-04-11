package com.DoAn.Web_QLDH_DichVu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendContactConfirmationEmail(String toEmail, String customerName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác nhận thông tin liên hệ - SMM Panel");
        message.setText("Xin chào " + customerName + ",\n\n"
                + "Cảm ơn bạn đã liên hệ với SMM Panel. Hệ thống đã ghi nhận thông tin đóng góp / câu hỏi của bạn.\n"
                + "Quản trị viên của chúng tôi sẽ xem xét và sớm phản hồi lại bạn qua email hoặc số điện thoại bạn đã cung cấp.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ hỗ trợ SMM Panel.");
        
        mailSender.send(message);
    }
}
