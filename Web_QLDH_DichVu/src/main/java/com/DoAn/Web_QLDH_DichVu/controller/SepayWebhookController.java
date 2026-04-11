package com.DoAn.Web_QLDH_DichVu.controller;

import com.DoAn.Web_QLDH_DichVu.service.SepayWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sepay")
public class SepayWebhookController {

    private final SepayWebhookService sepayWebhookService;

    public SepayWebhookController(SepayWebhookService sepayWebhookService) {
        this.sepayWebhookService = sepayWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestParam(value = "token", required = false) String token
    ) {
        // Nên bật token bí mật để tránh người ngoài tự gọi API
        String secretToken = "abc123sepay";
        if (token == null || !secretToken.equals(token)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        String result = sepayWebhookService.processWebhook(payload);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", result);
        return ResponseEntity.ok(response);
    }
}