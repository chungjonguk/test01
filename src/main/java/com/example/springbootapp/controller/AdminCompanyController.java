package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminCompanyController {

	@GetMapping({"/companies", "/companies.html"})
	public String companies() {
		return "admin/companies";
	}
}
