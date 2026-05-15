package com.example.springbootapp.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 홈 페이지
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Falcon | 대시보드 및 웹 앱 템플릿");
        return "index";
    }

    // Dashboard - Default
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "대시보드 - 기본");
        return "index";
    }
}
