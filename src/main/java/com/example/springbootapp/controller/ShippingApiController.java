package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.dto.WaybillIssueRequestDto;
import com.example.springbootapp.service.ShippingWaybillService;
import jakarta.servlet.http.HttpSession;

/**
 * 택배 운송장 REST API — {@code /api/shipping} (URL 암호화 제외).
 */
@RestController
@RequestMapping("/api/shipping")
public class ShippingApiController {

	private final ShippingWaybillService shippingWaybillService;

	public ShippingApiController(ShippingWaybillService shippingWaybillService) {
		this.shippingWaybillService = shippingWaybillService;
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> status() {
		return ResponseEntity.ok(shippingWaybillService.status());
	}

	@GetMapping("/orders/{orderId}")
	public ResponseEntity<Map<String, Object>> listByOrder(@PathVariable Long orderId) {
		try {
			List<Map<String, Object>> items = shippingWaybillService.listByOrder(orderId);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("items", items);
			body.put("count", items.size());
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PostMapping("/waybill")
	public ResponseEntity<Map<String, Object>> issueWaybill(
			@RequestBody WaybillIssueRequestDto dto,
			HttpSession session) {
		try {
			return ResponseEntity.ok(shippingWaybillService.issueWaybill(dto, session));
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
}
