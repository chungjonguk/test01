package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 업체별 도메인 등록 화면 — {@code /admin/company-domains}
 */
@Controller
@RequestMapping("/admin")
public class AdminCompanyDomainController {

	@GetMapping({"/company-domains", "/company-domains.html"})
	public String companyDomains() {
		return "admin/company-domains";
	}
}
