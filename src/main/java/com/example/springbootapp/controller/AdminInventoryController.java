package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 화면 경로: {@code /admin/inventory}
 * <p>상품(ecm_product) 재고 수량 조회·조정 화면.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminInventoryController {

	/**
	 * @return out: Thymeleaf view path {@code admin/inventory}
	 */
	@GetMapping({"/inventory", "/inventory.html"})
	public String inventory() {
		return "admin/inventory";
	}
}
