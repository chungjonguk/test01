package com.example.springbootapp.shipping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import com.example.springbootapp.config.ShippingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 택배사 공통: 미설정 시 목(Mock) 운송장, 설정 시 HTTP 연동 골격.
 */
abstract class AbstractCarrierWaybillClient implements CarrierWaybillClient {

	private static final ObjectMapper JSON = new ObjectMapper();

	protected final ShippingProperties properties;

	protected AbstractCarrierWaybillClient(ShippingProperties properties) {
		this.properties = properties;
	}

	protected boolean useMock() {
		return properties.isMockEnabled() || !endpoint().isConfigured();
	}

	protected abstract ShippingProperties.CarrierEndpoint endpoint();

	protected abstract String mockInvoicePrefix();

	protected WaybillIssueResult mockIssue(WaybillIssueCommand command) {
		String invoice = generateMockInvoice(mockInvoicePrefix());
		WaybillIssueResult result = new WaybillIssueResult();
		result.setSuccess(true);
		result.setMock(true);
		result.setInvoiceNo(invoice);
		result.setMessage(carrier().getLabel() + " 목 운송장이 발급되었습니다.");
		result.setRequestPayload(toJson(buildRequestBody(command)));
		Map<String, Object> resp = new LinkedHashMap<>();
		resp.put("mock", true);
		resp.put("invoiceNo", invoice);
		resp.put("carrier", carrier().getCode());
		result.setResponsePayload(toJson(resp));
		return result;
	}

	protected WaybillIssueResult remoteIssue(WaybillIssueCommand command) {
		WaybillIssueResult result = new WaybillIssueResult();
		result.setRequestPayload(toJson(buildRequestBody(command)));
		result.setSuccess(false);
		result.setMock(false);
		result.setMessage(
				carrier().getLabel()
						+ " API URL·키가 설정되었으나, 계약 스펙에 맞는 파서 연동이 필요합니다. app.shipping."
						+ carrier().getCode().toLowerCase()
						+ ".* 를 확인하세요.");
		return result;
	}

	protected Map<String, Object> buildRequestBody(WaybillIssueCommand command) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("orderNo", command.getOrderNo());
		body.put("carrier", carrier().getCode());
		body.put("senderNm", command.getSenderNm());
		body.put("senderPhone", command.getSenderPhone());
		body.put("senderZipcode", command.getSenderZipcode());
		body.put("senderAddress", command.getSenderAddress());
		body.put("recipientNm", command.getRecipientNm());
		body.put("recipientPhone", command.getRecipientPhone());
		body.put("zipcode", command.getZipcode());
		body.put("address", command.getAddress());
		body.put("addressDetail", command.getAddressDetail());
		body.put("boxCnt", command.getBoxCnt());
		body.put("weightKg", command.getWeightKg());
		body.put("goodsNm", command.getGoodsNm());
		body.put("customerId", endpoint().getCustomerId());
		return body;
	}

	@Override
	public WaybillIssueResult issue(WaybillIssueCommand command) {
		if (useMock()) {
			return mockIssue(command);
		}
		return remoteIssue(command);
	}

	private String generateMockInvoice(String prefix) {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
		int rnd = ThreadLocalRandom.current().nextInt(1000, 9999);
		return prefix + ts + rnd;
	}

	private String toJson(Object value) {
		try {
			return JSON.writeValueAsString(value);
		} catch (Exception ex) {
			return "{}";
		}
	}
}
