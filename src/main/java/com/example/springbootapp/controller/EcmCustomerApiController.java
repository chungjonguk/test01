package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.service.EcmCustomerService;

/**
 * 고객 REST API — {@code /api/e-commerce/customers}
 */
@RestController
@RequestMapping("/api/e-commerce/customers")
public class EcmCustomerApiController {

	private final EcmCustomerService ecmCustomerService;

	public EcmCustomerApiController(EcmCustomerService ecmCustomerService) {
		this.ecmCustomerService = ecmCustomerService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(@RequestParam(required = false) String keyword) {
		List<Map<String, Object>> items = ecmCustomerService.searchForGrid(keyword);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("items", items);
		body.put("count", items.size());
		return ResponseEntity.ok(body);
	}
}
