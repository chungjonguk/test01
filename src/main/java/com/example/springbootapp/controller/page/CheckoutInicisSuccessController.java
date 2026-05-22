package com.example.springbootapp.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.springbootapp.domain.EcmPayment;
import com.example.springbootapp.mapper.EcmPaymentMapper;

@Controller
@RequestMapping("/app/e-commerce/checkout/inicis")
public class CheckoutInicisSuccessController {

	private final EcmPaymentMapper ecmPaymentMapper;

	public CheckoutInicisSuccessController(EcmPaymentMapper ecmPaymentMapper) {
		this.ecmPaymentMapper = ecmPaymentMapper;
	}

	@GetMapping("/success")
	public String success(@RequestParam(required = false) String orderNo, Model model) {
		if (orderNo != null && !orderNo.isBlank()) {
			EcmPayment payment = ecmPaymentMapper.findByOrderNo(orderNo);
			if (payment != null) {
				model.addAttribute("paymentResult", java.util.Map.of(
						"success", true,
						"message", "결제가 완료되었습니다.",
						"orderNo", payment.getOrderNo(),
						"tid", payment.getTid() != null ? payment.getTid() : "",
						"amount", payment.getAmount(),
						"statusCd", payment.getStatusCd()));
				return "app/e-commerce/checkout-inicis-success";
			}
		}
		model.addAttribute("paymentResult", java.util.Map.of(
				"success", true,
				"message", "결제가 완료되었습니다."));
		return "app/e-commerce/checkout-inicis-success";
	}
}
