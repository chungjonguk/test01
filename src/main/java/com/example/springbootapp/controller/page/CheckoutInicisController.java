package com.example.springbootapp.controller.page;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.springbootapp.service.InicisPaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/app/e-commerce/checkout/inicis")
public class CheckoutInicisController {

	private final InicisPaymentService inicisPaymentService;

	public CheckoutInicisController(InicisPaymentService inicisPaymentService) {
		this.inicisPaymentService = inicisPaymentService;
	}

	@PostMapping("/return")
	public String returnFromInicis(HttpServletRequest request, HttpSession session, Model model) {
		Map<String, String> params = extractParams(request);
		try {
			Map<String, Object> result = inicisPaymentService.handleReturn(params, session);
			model.addAttribute("paymentResult", result);
			return Boolean.TRUE.equals(result.get("success"))
					? "app/e-commerce/checkout-inicis-success"
					: "app/e-commerce/checkout-inicis-fail";
		} catch (Exception ex) {
			model.addAttribute("paymentResult", Map.of(
					"success", false,
					"message", ex.getMessage() != null ? ex.getMessage() : "결제 처리 오류"));
			return "app/e-commerce/checkout-inicis-fail";
		}
	}

	@GetMapping("/return")
	public String returnGet(HttpServletRequest request, HttpSession session, Model model) {
		return returnFromInicis(request, session, model);
	}

	@GetMapping("/close")
	public String close(Model model) {
		model.addAttribute("paymentResult", Map.of(
				"success", false,
				"message", "결제가 취소되었습니다."));
		return "app/e-commerce/checkout-inicis-fail";
	}

	private static Map<String, String> extractParams(HttpServletRequest request) {
		Map<String, String> params = new LinkedHashMap<>();
		request.getParameterMap().forEach((key, values) -> {
			if (values != null && values.length > 0) {
				params.put(key, values[0]);
			}
		});
		return params;
	}
}
