package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminCodeController {

    @GetMapping({"/codes", "/codes.html"})
    public String codes(Model model) {
        model.addAttribute("title", "코드 관리");
        model.addAttribute("loadCodeManagementActions", true);
        return "admin/codes";
    }
}
