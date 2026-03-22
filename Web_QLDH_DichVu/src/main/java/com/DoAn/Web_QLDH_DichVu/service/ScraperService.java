package com.DoAn.Web_QLDH_DichVu.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

    public int getInitialCount(String targetLink) {
        try {
            // Dùng Jsoup kết nối đến link mục tiêu
            Document doc = Jsoup.connect(targetLink)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(5000)
                    .get();

            // Lưu ý: Các mạng xã hội lớn (FB, TikTok) chống bot rất gắt.
            // Để demo đồ án, nếu không lấy được thẻ HTML, ta giả lập trả về một con số ngẫu nhiên.
            // VD: String countText = doc.select(".like-count").text();

            return 1250; // Trả về số giả định để test luồng

        } catch (Exception e) {
            System.err.println("Lỗi Jsoup: " + e.getMessage());
            return 0;
        }
    }
}