package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminMenuController {

	@GetMapping({"/menus", "/menus.html"})
	public String menus() {
		return "admin/menus";
	}
}
