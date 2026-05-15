package com.example.springbootapp.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentationController {

    @GetMapping("/documentation/getting-started")
    public String documentationGettingStarted(Model model) {
        model.addAttribute("title", "시작하기");
        return "documentation/getting-started";
    }

    @GetMapping("/documentation/customization/configuration")
    public String documentationCustomizationConfiguration(Model model) {
        model.addAttribute("title", "설정");
        return "documentation/customization/configuration";
    }

    @GetMapping("/documentation/customization/styling")
    public String documentationCustomizationStyling(Model model) {
        model.addAttribute("title", "스타일");
        return "documentation/customization/styling";
    }

    @GetMapping("/documentation/customization/dark-mode")
    public String documentationCustomizationDarkMode(Model model) {
        model.addAttribute("title", "다크 모드");
        return "documentation/customization/dark-mode";
    }

    @GetMapping("/documentation/customization/plugin")
    public String documentationCustomizationPlugin(Model model) {
        model.addAttribute("title", "플러그인");
        return "documentation/customization/plugin";
    }

    @GetMapping("/documentation/faq")
    public String documentationFaq(Model model) {
        model.addAttribute("title", "FAQ");
        return "documentation/faq";
    }

    @GetMapping("/documentation/gulp")
    public String documentationGulp(Model model) {
        model.addAttribute("title", "Gulp");
        return "documentation/gulp";
    }

    @GetMapping("/documentation/design-file")
    public String documentationDesignFile(Model model) {
        model.addAttribute("title", "디자인 파일");
        return "documentation/design-file";
    }
}
