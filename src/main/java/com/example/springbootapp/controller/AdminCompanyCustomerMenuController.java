package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminCompanyCustomerMenuController {

	@GetMapping({"/admin/company-customer-menus", "/admin/company-customer-menus.html"})
	public String page(Model model) {
		model.addAttribute("title", "고객 노출 메뉴");
		return "admin/company-customer-menus";
	}
}
