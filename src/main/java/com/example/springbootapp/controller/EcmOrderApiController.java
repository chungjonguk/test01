package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.service.EcmOrderService;

/**
 * 주문 REST API — {@code /api/e-commerce/orders}
 */
@RestController
@RequestMapping("/api/e-commerce/orders")
public class EcmOrderApiController {

	private final EcmOrderService ecmOrderService;

	public EcmOrderApiController(EcmOrderService ecmOrderService) {
		this.ecmOrderService = ecmOrderService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String statusCd) {
		List<Map<String, Object>> items = ecmOrderService.searchForGrid(keyword, statusCd);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("items", items);
		body.put("count", items.size());
		return ResponseEntity.ok(body);
	}
}
