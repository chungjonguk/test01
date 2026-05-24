package com.example.springbootapp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * 화면 경로: {@code /admin/menus}
 * <p>메뉴 관리 화면을 렌더링합니다.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminMenuController {
	/**
	 * @return out: Thymeleaf view path {@code admin/menus}
	 */
	@GetMapping({"/menus", "/menus.html"})
	public String menus() {
		return "admin/menus";
	}
}
