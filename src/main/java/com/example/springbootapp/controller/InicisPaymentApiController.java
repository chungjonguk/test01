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
/**
 * KG 이니시스 결제 REST API.
 * <p>기본 경로: {@code /api/payments/inicis}</p>
 */
@RestController
@RequestMapping("/api/payments/inicis")
public class InicisPaymentApiController {
	private final InicisPaymentService inicisPaymentService;
	private final InicisProperties inicisProperties;
	public InicisPaymentApiController(InicisPaymentService inicisPaymentService, InicisProperties inicisProperties) {
		this.inicisPaymentService = inicisPaymentService;
		this.inicisProperties = inicisProperties;
	}
	/**
	 * 이니시스 결제 연동 설정 정보를 조회합니다.
	 *
	 * @return out: {@code ResponseEntity<Map>} — {@code enabled}, {@code mockEnabled}, {@code mid}, {@code returnUrl} 등
	 */
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
	/**
	 * 결제 요청을 준비하고 결제창 호출에 필요한 파라미터를 반환합니다.
	 *
	 * @param request in: 결제 준비 요청 (주문번호, 금액 등)
	 * @param session in: 주문·세션 연동용 HTTP 세션
	 * @return out: {@code ResponseEntity<Map>} — 결제 파라미터, {@code success}, {@code useRealGateway} 또는 400
	 */
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
	/**
	 * 목(mock) 결제를 완료 처리합니다.
	 *
	 * @param orderNo in: 주문번호
	 * @param session in: 주문 검증용 HTTP 세션
	 * @return out: {@code ResponseEntity<Map>} — 결제 완료 결과 또는 400
	 */
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
