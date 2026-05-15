package com.example.springbootapp.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MiscController {

    @GetMapping("/changelog")
    public String changelog(Model model) {
        model.addAttribute("title", "변경 이력");
        return "changelog";
    }

    @GetMapping("/widgets")
    public String widgets(Model model) {
        model.addAttribute("title", "위젯");
        return "widgets";
    }
}
