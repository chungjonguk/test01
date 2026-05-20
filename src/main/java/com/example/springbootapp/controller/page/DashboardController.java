package com.example.springbootapp.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard/analytics")
    public String dashboardAnalytics() {
        return "dashboard/analytics";
    }

    @GetMapping("/dashboard/crm")
    public String dashboardCrm() {
        return "dashboard/crm";
    }

    @GetMapping("/dashboard/e-commerce")
    public String dashboardEcommerce() {
        return "dashboard/e-commerce";
    }

    @GetMapping("/dashboard/lms")
    public String dashboardLms() {
        return "dashboard/lms";
    }

    @GetMapping("/dashboard/project-management")
    public String dashboardProjectManagement() {
        return "dashboard/project-management";
    }

    @GetMapping("/dashboard/saas")
    public String dashboardSaas() {
        return "dashboard/saas";
    }
}
