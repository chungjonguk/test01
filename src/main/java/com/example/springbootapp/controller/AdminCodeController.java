package com.example.springbootapp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * 화면 경로: {@code /admin/codes}
 * <p>공통 코드(코드그룹·상세코드) 관리 화면을 렌더링합니다.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminCodeController {
	/**
	 * @return out: Thymeleaf view path {@code admin/codes}
	 */
	@GetMapping({"/codes", "/codes.html"})
	public String codes() {
		return "admin/codes";
	}
}
