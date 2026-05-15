package com.example.springbootapp.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping("/dashboard/analytics")
    public String dashboardAnalytics(Model model) {
        log.info("{} - dashboard analytics 호출", DashboardController.class.getSimpleName());
        model.addAttribute("title", "대시보드 - 분석");
        return "dashboard/analytics";
    }

    @GetMapping("/dashboard/crm")
    public String dashboardCrm(Model model) {
        log.info("{} - dashboard crm 호출", DashboardController.class.getSimpleName());        
        model.addAttribute("title", "대시보드 - CRM");
        return "dashboard/crm";
    }

    @GetMapping("/dashboard/e-commerce")
    public String dashboardEcommerce(Model model) {
        log.info("{} - dashboard e-commerce 호출", DashboardController.class.getSimpleName());                
        model.addAttribute("title", "대시보드 - 이커머스");
        return "dashboard/e-commerce";
    }

    @GetMapping("/dashboard/lms")
    public String dashboardLms(Model model) {
        log.info("{} - dashboard lms 호출", DashboardController.class.getSimpleName());                        
        model.addAttribute("title", "대시보드 - LMS");
        return "dashboard/lms";
    }

    @GetMapping("/dashboard/project-management")
    public String dashboardProjectManagement(Model model) {
        log.info("{} - dashboard project-management 호출", DashboardController.class.getSimpleName());                                
        model.addAttribute("title", "대시보드 - 프로젝트 관리");
        return "dashboard/project-management";
    }

    @GetMapping("/dashboard/saas")
    public String dashboardSaas(Model model) {
        log.info("{} - dashboard saas 호출", DashboardController.class.getSimpleName());                                        
        model.addAttribute("title", "대시보드 - SaaS");
        return "dashboard/saas";
    }
}
