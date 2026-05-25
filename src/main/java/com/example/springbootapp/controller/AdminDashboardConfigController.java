package com.example.springbootapp.controller;

import com.example.springbootapp.config.DashboardWidgetCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 홈 대시보드 위젯 구성 화면.
 * <p>경로: {@code /admin/dashboard-config}</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardConfigController {

	@GetMapping({"/dashboard-config", "/dashboard-config.html"})
	public String dashboardConfig(Model model) {
		model.addAttribute("title", "대시보드 구성");
		model.addAttribute("dashboardWidgetCatalog", DashboardWidgetCatalog.toMaps(DashboardWidgetCatalog.all()));
		model.addAttribute("dashboardDefaultIds", DashboardWidgetCatalog.defaultEnabledIds());
		return "admin/dashboard-config";
	}
}
