package com.example.springbootapp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * 화면 경로: {@code /admin/companies}
 * <p>업체(biz_company) 관리 화면을 렌더링합니다.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminCompanyController {
	/**
	 * @return out: Thymeleaf view path {@code admin/companies}
	 */
	@GetMapping({"/companies", "/companies.html"})
	public String companies() {
		return "admin/companies";
	}
}
