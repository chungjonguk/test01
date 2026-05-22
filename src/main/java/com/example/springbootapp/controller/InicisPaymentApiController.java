package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.config.InicisProperties;
import com.example.springbootapp.dto.InicisPrepareRequest;
import com.example.springbootapp.service.InicisPaymentService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/payments/inicis")
public class InicisPaymentApiController {

	private final InicisPaymentService inicisPaymentService;
	private final InicisProperties inicisProperties;

	public InicisPaymentApiController(InicisPaymentService inicisPaymentService, InicisProperties inicisProperties) {
		this.inicisPaymentService = inicisPaymentService;
		this.inicisProperties = inicisProperties;
	}

	@GetMapping("/config")
	public ResponseEntity<Map<String, Object>> config() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("enabled", inicisProperties.isEnabled());
		body.put("mockEnabled", inicisProperties.isMockEnabled());
		body.put("useRealGateway", inicisProperties.useRealGateway());
		body.put("mid", inicisProperties.getMid());
		body.put("stdPayJsUrl", inicisProperties.getStdPayJsUrl());
		body.put("returnUrl", inicisProperties.returnUrl());
		return ResponseEntity.ok(body);
	}

	@PostMapping("/prepare")
	public ResponseEntity<Map<String, Object>> prepare(@RequestBody InicisPrepareRequest request, HttpSession session) {
		try {
			Map<String, Object> body = new LinkedHashMap<>(inicisPaymentService.prepare(request, session));
			body.put("success", true);
			body.put("useRealGateway", inicisProperties.useRealGateway());
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PostMapping("/mock-complete")
	public ResponseEntity<Map<String, Object>> mockComplete(@RequestParam String orderNo, HttpSession session) {
		try {
			return ResponseEntity.ok(inicisPaymentService.completeMock(orderNo, session));
		} catch (IllegalArgumentException | IllegalStateException ex) {
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
