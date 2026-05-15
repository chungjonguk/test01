package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {

    @GetMapping("/demo/combo-nav")
    public String demoComboNav(Model model) {
        model.addAttribute("title", "콤보 내비게이션");
        return "demo/combo-nav";
    }

    @GetMapping("/demo/fluid")
    public String demoFluid(Model model) {
        model.addAttribute("title", "유동");
        return "demo/fluid";
    }

    @GetMapping("/demo/navbar-top")
    public String demoNavbarTop(Model model) {
        model.addAttribute("title", "상단 내비게이션");
        return "demo/navbar-top";
    }

    @GetMapping("/demo/navbar-vertical-card")
    public String demoNavbarVerticalCard(Model model) {
        model.addAttribute("title", "세로 내비게이션 카드");
        return "demo/navbar-vertical-card";
    }

    @GetMapping("/demo/navbar-vertical-inverted")
    public String demoNavbarVerticalInverted(Model model) {
        model.addAttribute("title", "세로 내비게이션 반전");
        return "demo/navbar-vertical-inverted";
    }

    @GetMapping("/demo/navbar-vertical-vibrant")
    public String demoNavbarVerticalVibrant(Model model) {
        model.addAttribute("title", "세로 내비게이션 비비드");
        return "demo/navbar-vertical-vibrant";
    }

    @GetMapping("/demo/RTL")
    public String demoRTL(Model model) {
        model.addAttribute("title", "RTL");
        return "demo/RTL";
    }
}
