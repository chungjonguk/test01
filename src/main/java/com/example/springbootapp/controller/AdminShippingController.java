package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 운송장 발급 화면 — {@code /admin/shipping}
 */
@Controller
@RequestMapping("/admin")
public class AdminShippingController {

	@GetMapping({"/shipping", "/shipping.html"})
	public String shipping() {
		return "admin/shipping";
	}
}
