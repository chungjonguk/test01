package com.example.springbootapp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.InicisProperties;
import com.example.springbootapp.domain.EcmPayment;
import com.example.springbootapp.dto.InicisPrepareRequest;
import com.example.springbootapp.mapper.EcmPaymentMapper;
import com.example.springbootapp.payment.InicisSignatureHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Service
public class InicisPaymentService {

	private static final Logger log = LoggerFactory.getLogger(InicisPaymentService.class);

	private final InicisProperties inicisProperties;
	private final EcmPaymentMapper ecmPaymentMapper;
	private final CheckoutOrderService checkoutOrderService;
	private final SessionAuthService sessionAuthService;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	public InicisPaymentService(
			InicisProperties inicisProperties,
			EcmPaymentMapper ecmPaymentMapper,
			CheckoutOrderService checkoutOrderService,
			SessionAuthService sessionAuthService,
			RestTemplate restTemplate,
			ObjectMapper objectMapper) {
		this.inicisProperties = inicisProperties;
		this.ecmPaymentMapper = ecmPaymentMapper;
		this.checkoutOrderService = checkoutOrderService;
		this.sessionAuthService = sessionAuthService;
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public Map<String, Object> prepare(InicisPrepareRequest request, HttpSession session) {
		validatePrepare(request);
		String actor = resolveActor(session);
		BigDecimal amount = request.getAmount().setScale(0, RoundingMode.HALF_UP);
		String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);

		EcmPayment payment = new EcmPayment();
		payment.setOrderNo(orderNo);
		payment.setOrderId(request.getOrderId());
		payment.setPgCd("INICIS");
		payment.setMid(inicisProperties.getMid());
		payment.setAmount(amount);
		payment.setCurrencyCd("WON");
		payment.setGoodName(trim(request.getGoodName(), 200));
		payment.setBuyerName(trim(request.getBuyerName(), 100));
		payment.setBuyerTel(trim(request.getBuyerTel(), 30));
		payment.setBuyerEmail(trim(request.getBuyerEmail(), 200));
		payment.setStatusCd("READY");
		payment.setRegId(actor);
		payment.setUpdateId(actor);
		ecmPaymentMapper.insert(payment);
		Long orderId = checkoutOrderService.createPendingOrder(
				orderNo, amount, request.getCustomerId(), request.getShipTo(), actor);
		payment.setOrderId(orderId);

		if (!inicisProperties.useRealGateway()) {
			Map<String, Object> mock = new LinkedHashMap<>();
			mock.put("mock", true);
			mock.put("orderNo", orderNo);
			mock.put("amount", amount.toPlainString());
			mock.put("message", "로컬 모의 결제 모드입니다. mockEnabled=false 및 signKey 설정 시 이니시스 테스트창이 열립니다.");
			return mock;
		}

		String price = amount.toPlainString();
		String timestamp = String.valueOf(System.currentTimeMillis());
		String signKey = inicisProperties.getSignKey();

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("version", "1.0");
		params.put("gopaymethod", "");
		params.put("mid", inicisProperties.getMid());
		params.put("oid", orderNo);
		params.put("price", price);
		params.put("timestamp", timestamp);
		params.put("use_chkfake", "Y");
		params.put("signature", InicisSignatureHelper.paymentSignature(orderNo, price, timestamp));
		params.put("verification", InicisSignatureHelper.paymentVerification(orderNo, price, signKey, timestamp));
		params.put("mKey", InicisSignatureHelper.mKey(signKey));
		params.put("currency", "WON");
		params.put("goodname", payment.getGoodName());
		params.put("buyername", payment.getBuyerName() != null ? payment.getBuyerName() : "구매자");
		params.put("buyertel", payment.getBuyerTel() != null ? payment.getBuyerTel() : "01000000000");
		params.put("buyeremail", payment.getBuyerEmail() != null ? payment.getBuyerEmail() : "buyer@example.com");
		params.put("returnUrl", inicisProperties.returnUrl());
		params.put("closeUrl", inicisProperties.closeUrl());
		params.put("charset", "UTF-8");
		params.put("orderNo", orderNo);
		params.put("stdPayJsUrl", inicisProperties.getStdPayJsUrl());
		return params;
	}

	@Transactional
	public Map<String, Object> completeMock(String orderNo, HttpSession session) {
		EcmPayment payment = requirePayment(orderNo);
		if (inicisProperties.useRealGateway()) {
			throw new IllegalStateException("모의 결제는 mock 모드에서만 사용할 수 있습니다.");
		}
		String actor = resolveActor(session);
		payment.setStatusCd("PAID");
		payment.setResultCode("0000");
		payment.setResultMsg("모의 결제 승인");
		payment.setTid("MOCK-" + orderNo);
		payment.setRawApproveJson("{\"mock\":true}");
		payment.setUpdateId(actor);
		ecmPaymentMapper.updateAfterApprove(payment);
		checkoutOrderService.markOrderPaid(orderNo, actor);
		return resultMap(payment, true, "모의 결제가 완료되었습니다.");
	}

	@Transactional
	public Map<String, Object> handleReturn(Map<String, String> params, HttpSession session) {
		String orderNo = firstNonBlank(params.get("MOID"), params.get("oid"), params.get("orderNumber"));
		String resultCode = params.get("resultCode");
		String resultMsg = params.get("resultMsg");
		String authToken = params.get("authToken");
		String authUrl = params.get("authUrl");
		String idcName = params.get("idc_name");

		EcmPayment payment = orderNo != null ? ecmPaymentMapper.findByOrderNo(orderNo) : null;
		if (payment == null) {
			throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
		}

		String actor = resolveActor(session);
		payment.setResultCode(resultCode);
		payment.setResultMsg(resultMsg);
		payment.setAuthToken(authToken);
		payment.setIdcName(idcName);
		payment.setRawAuthJson(toJson(params));
		payment.setUpdateId(actor);

		if (!"0000".equals(resultCode)) {
			payment.setStatusCd("FAILED");
			ecmPaymentMapper.updateAfterAuth(payment);
			checkoutOrderService.markOrderFailed(orderNo, actor);
			return resultMap(payment, false, resultMsg != null ? resultMsg : "결제 인증에 실패했습니다.");
		}

		payment.setStatusCd("PENDING_AUTH");
		ecmPaymentMapper.updateAfterAuth(payment);

		if (authUrl == null || authUrl.isBlank() || authToken == null || authToken.isBlank()) {
			payment.setStatusCd("FAILED");
			payment.setResultMsg("authUrl 또는 authToken 없음");
			ecmPaymentMapper.updateAfterAuth(payment);
			return resultMap(payment, false, "승인 요청 정보가 없습니다.");
		}

		try {
			Map<String, String> approveResponse = requestApprove(authUrl, authToken, payment.getAmount());
			payment.setRawApproveJson(toJson(approveResponse));
			String approveCode = getMapValue(approveResponse, "resultCode");
			if ("0000".equals(approveCode)) {
				payment.setStatusCd("PAID");
				payment.setTid(getMapValue(approveResponse, "tid"));
				payment.setResultCode(approveCode);
				payment.setResultMsg(getMapValue(approveResponse, "resultMsg"));
				ecmPaymentMapper.updateAfterApprove(payment);
				checkoutOrderService.markOrderPaid(orderNo, actor);
				return resultMap(payment, true, "결제가 완료되었습니다.");
			}
			payment.setStatusCd("FAILED");
			payment.setResultCode(approveCode);
			payment.setResultMsg(getMapValue(approveResponse, "resultMsg"));
			ecmPaymentMapper.updateAfterApprove(payment);
			checkoutOrderService.markOrderFailed(orderNo, actor);
			return resultMap(payment, false, payment.getResultMsg() != null ? payment.getResultMsg() : "승인에 실패했습니다.");
		} catch (Exception ex) {
			log.error("이니시스 승인 API 호출 실패: orderNo={}", orderNo, ex);
			payment.setStatusCd("FAILED");
			payment.setResultMsg(ex.getMessage());
			ecmPaymentMapper.updateAfterAuth(payment);
			checkoutOrderService.markOrderFailed(orderNo, actor);
			return resultMap(payment, false, "승인 API 호출 중 오류가 발생했습니다.");
		}
	}

	private Map<String, String> requestApprove(String authUrl, String authToken, BigDecimal amount) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		String signKey = inicisProperties.getSignKey();
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("mid", inicisProperties.getMid());
		body.add("authToken", authToken);
		body.add("timestamp", timestamp);
		body.add("signature", InicisSignatureHelper.approveSignature(authToken, timestamp));
		body.add("verification", InicisSignatureHelper.approveVerification(authToken, signKey, timestamp));
		body.add("charset", "UTF-8");
		body.add("format", "JSON");
		body.add("price", amount.setScale(0, RoundingMode.HALF_UP).toPlainString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(authUrl, entity, String.class);
		return parseResponseBody(response.getBody());
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> parseResponseBody(String body) {
		if (body == null || body.isBlank()) {
			return Map.of();
		}
		try {
			if (body.trim().startsWith("{")) {
				return objectMapper.readValue(body, Map.class);
			}
			Map<String, String> map = new LinkedHashMap<>();
			for (String pair : body.split("&")) {
				int idx = pair.indexOf('=');
				if (idx > 0) {
					map.put(pair.substring(0, idx), pair.substring(idx + 1));
				}
			}
			return map;
		} catch (Exception ex) {
			log.warn("승인 응답 파싱 실패: {}", body);
			return Map.of("rawBody", body);
		}
	}

	private EcmPayment requirePayment(String orderNo) {
		if (orderNo == null || orderNo.isBlank()) {
			throw new IllegalArgumentException("주문번호가 없습니다.");
		}
		EcmPayment payment = ecmPaymentMapper.findByOrderNo(orderNo);
		if (payment == null) {
			throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
		}
		return payment;
	}

	private void validatePrepare(InicisPrepareRequest request) {
		if (request == null || request.getAmount() == null || request.getAmount().signum() <= 0) {
			throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
		}
		if (request.getGoodName() == null || request.getGoodName().isBlank()) {
			throw new IllegalArgumentException("상품명을 입력해 주세요.");
		}
	}

	private Map<String, Object> resultMap(EcmPayment payment, boolean success, String message) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("success", success);
		map.put("message", message);
		map.put("orderNo", payment.getOrderNo());
		map.put("tid", payment.getTid());
		map.put("amount", payment.getAmount());
		map.put("statusCd", payment.getStatusCd());
		return map;
	}

	private String resolveActor(HttpSession session) {
		LoginSession login = sessionAuthService.getLoginSession(session);
		return login != null && login.getUserId() != null ? login.getUserId() : "SYSTEM";
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception ex) {
			return String.valueOf(value);
		}
	}

	private static String firstNonBlank(String... values) {
		for (String v : values) {
			if (v != null && !v.isBlank()) {
				return v;
			}
		}
		return null;
	}

	private static String getMapValue(Map<String, ?> map, String key) {
		if (map == null || key == null) {
			return null;
		}
		Object direct = map.get(key);
		if (direct != null) {
			return String.valueOf(direct);
		}
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			if (key.equalsIgnoreCase(entry.getKey()) && entry.getValue() != null) {
				return String.valueOf(entry.getValue());
			}
		}
		return null;
	}

	private static String trim(String value, int max) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		return t.length() <= max ? t : t.substring(0, max);
	}
}
