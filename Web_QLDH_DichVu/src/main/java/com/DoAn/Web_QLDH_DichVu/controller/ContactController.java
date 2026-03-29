package com.DoAn.Web_QLDH_DichVu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    @GetMapping("/contact")
    public String contactPage() {
        return "contact"; // trỏ tới templates/contact.html
    }
}