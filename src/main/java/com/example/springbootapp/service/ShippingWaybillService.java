package com.example.springbootapp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.ShippingProperties;
import com.example.springbootapp.domain.EcmCustomer;
import com.example.springbootapp.domain.EcmOrder;
import com.example.springbootapp.domain.EcmShipment;
import com.example.springbootapp.dto.WaybillIssueRequestDto;
import com.example.springbootapp.mapper.EcmCustomerMapper;
import com.example.springbootapp.mapper.EcmOrderMapper;
import com.example.springbootapp.mapper.EcmShipmentMapper;
import com.example.springbootapp.shipping.CarrierCd;
import com.example.springbootapp.shipping.CarrierWaybillRouter;
import com.example.springbootapp.shipping.WaybillIssueCommand;
import com.example.springbootapp.shipping.WaybillIssueResult;
import jakarta.servlet.http.HttpSession;

@Service
public class ShippingWaybillService {

	public static final String STATUS_ISSUED = "ISSUED";
	public static final String STATUS_FAILED = "FAILED";
	public static final String ORDER_SHIPPED = "SHIPPED";

	private final ShippingProperties shippingProperties;
	private final CarrierWaybillRouter carrierRouter;
	private final EcmOrderMapper ecmOrderMapper;
	private final EcmCustomerMapper ecmCustomerMapper;
	private final EcmShipmentMapper ecmShipmentMapper;
	private final SessionAuthService sessionAuthService;

	public ShippingWaybillService(
			ShippingProperties shippingProperties,
			CarrierWaybillRouter carrierRouter,
			EcmOrderMapper ecmOrderMapper,
			EcmCustomerMapper ecmCustomerMapper,
			EcmShipmentMapper ecmShipmentMapper,
			SessionAuthService sessionAuthService) {
		this.shippingProperties = shippingProperties;
		this.carrierRouter = carrierRouter;
		this.ecmOrderMapper = ecmOrderMapper;
		this.ecmCustomerMapper = ecmCustomerMapper;
		this.ecmShipmentMapper = ecmShipmentMapper;
		this.sessionAuthService = sessionAuthService;
	}

	public Map<String, Object> status() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("enabled", shippingProperties.isEnabled());
		body.put("mockEnabled", shippingProperties.isMockEnabled());
		List<Map<String, Object>> carriers = new ArrayList<>();
		for (CarrierCd cd : CarrierCd.values()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("code", cd.getCode());
			row.put("label", cd.getLabel());
			row.put("configured", isCarrierConfigured(cd));
			carriers.add(row);
		}
		body.put("carriers", carriers);
		return body;
	}

	public List<Map<String, Object>> listByOrder(Long orderId) {
		if (orderId == null) {
			throw new IllegalArgumentException("주문 ID가 필요합니다.");
		}
		return ecmShipmentMapper.findByOrderId(orderId).stream().map(this::toRow).toList();
	}

	@Transactional
	public Map<String, Object> issueWaybill(WaybillIssueRequestDto dto, HttpSession session) {
		if (!shippingProperties.isEnabled()) {
			throw new IllegalArgumentException("배송 연동이 비활성화되어 있습니다.");
		}
		if (dto == null || dto.getOrderId() == null) {
			throw new IllegalArgumentException("주문 ID가 필요합니다.");
		}
		CarrierCd carrier = CarrierCd.fromCode(dto.getCarrierCd())
				.orElseThrow(() -> new IllegalArgumentException("택배사는 CJ, EPOST, LOTTE 중 하나여야 합니다."));
		EcmOrder order = ecmOrderMapper.findById(dto.getOrderId());
		if (order == null) {
			throw new IllegalArgumentException("주문을 찾을 수 없습니다.");
		}
		if (ecmShipmentMapper.countIssuedByOrderId(order.getOrderId()) > 0) {
			throw new IllegalArgumentException("이미 운송장이 발급된 주문입니다.");
		}
		EcmCustomer customer = ecmCustomerMapper.findById(order.getCustomerId());
		if (customer == null) {
			throw new IllegalArgumentException("고객 정보를 찾을 수 없습니다.");
		}
		String actor = resolveActor(session);
		WaybillIssueCommand command = buildCommand(order, customer, carrier, dto);
		WaybillIssueResult apiResult = carrierRouter.resolve(carrier).issue(command);
		EcmShipment shipment = new EcmShipment();
		shipment.setOrderId(order.getOrderId());
		shipment.setCarrierCd(carrier.getCode());
		shipment.setBoxCnt(command.getBoxCnt());
		shipment.setWeightKg(command.getWeightKg());
		shipment.setRecipientNm(command.getRecipientNm());
		shipment.setRecipientPhone(command.getRecipientPhone());
		shipment.setZipcode(command.getZipcode());
		shipment.setAddress(command.getAddress());
		shipment.setAddressDetail(command.getAddressDetail());
		shipment.setRequestPayload(apiResult.getRequestPayload());
		shipment.setResponsePayload(apiResult.getResponsePayload());
		shipment.setRegId(actor);
		shipment.setUpdateId(actor);
		if (apiResult.isSuccess() && apiResult.getInvoiceNo() != null && !apiResult.getInvoiceNo().isBlank()) {
			shipment.setInvoiceNo(apiResult.getInvoiceNo());
			shipment.setStatusCd(STATUS_ISSUED);
			shipment.setIssuedDt(LocalDateTime.now());
		} else {
			shipment.setStatusCd(STATUS_FAILED);
		}
		ecmShipmentMapper.insert(shipment);
		if (STATUS_ISSUED.equals(shipment.getStatusCd())) {
			ecmOrderMapper.updateStatusByOrderId(order.getOrderId(), ORDER_SHIPPED, actor);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", apiResult.isSuccess());
		body.put("mock", apiResult.isMock());
		body.put("message", apiResult.getMessage());
		body.put("shipment", toRow(shipment));
		body.put("orderStatus", STATUS_ISSUED.equals(shipment.getStatusCd()) ? ORDER_SHIPPED : order.getStatusCd());
		return body;
	}

	private WaybillIssueCommand buildCommand(
			EcmOrder order,
			EcmCustomer customer,
			CarrierCd carrier,
			WaybillIssueRequestDto dto) {
		ShippingProperties.Sender sender = shippingProperties.getSender();
		WaybillIssueCommand command = new WaybillIssueCommand();
		command.setOrderNo(order.getOrderNo());
		command.setCarrierCd(carrier);
		command.setSenderNm(sender.getName());
		command.setSenderPhone(sender.getPhone());
		command.setSenderZipcode(sender.getZipcode());
		command.setSenderAddress(sender.getAddress());
		command.setRecipientNm(nonBlank(customer.getCustomerNm(), "수령인"));
		command.setRecipientPhone(nonBlank(customer.getPhone(), "01000000000"));
		String zip = nonBlank(customer.getZipcode(), "00000");
		String addr = nonBlank(customer.getAddress(), null);
		String detail = customer.getAddressDetail();
		if (addr == null || addr.isBlank()) {
			addr = nonBlank(order.getShipTo(), "배송지 미입력");
			detail = null;
		}
		command.setZipcode(zip);
		command.setAddress(addr);
		command.setAddressDetail(detail);
		int boxCnt = dto.getBoxCnt() != null && dto.getBoxCnt() > 0 ? dto.getBoxCnt() : 1;
		command.setBoxCnt(boxCnt);
		command.setWeightKg(dto.getWeightKg() != null ? dto.getWeightKg() : BigDecimal.ONE);
		command.setGoodsNm("PrintMall 주문 " + order.getOrderNo());
		return command;
	}

	private boolean isCarrierConfigured(CarrierCd cd) {
		return switch (cd) {
			case CJ -> shippingProperties.getCj().isConfigured();
			case EPOST -> shippingProperties.getEpost().isConfigured();
			case LOTTE -> shippingProperties.getLotte().isConfigured();
		};
	}

	private Map<String, Object> toRow(EcmShipment s) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("shipmentId", s.getShipmentId());
		row.put("orderId", s.getOrderId());
		row.put("carrierCd", s.getCarrierCd());
		CarrierCd.fromCode(s.getCarrierCd()).ifPresent(cd -> row.put("carrierLabel", cd.getLabel()));
		row.put("invoiceNo", s.getInvoiceNo());
		row.put("statusCd", s.getStatusCd());
		row.put("recipientNm", s.getRecipientNm());
		row.put("recipientPhone", s.getRecipientPhone());
		row.put("zipcode", s.getZipcode());
		row.put("address", s.getAddress());
		row.put("addressDetail", s.getAddressDetail());
		row.put("boxCnt", s.getBoxCnt());
		row.put("weightKg", s.getWeightKg());
		row.put("issuedDt", s.getIssuedDt());
		row.put("regDt", s.getRegDt());
		return row;
	}

	private String resolveActor(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		return userId != null && !userId.isBlank() ? userId : "SYSTEM";
	}

	private static String nonBlank(String value, String fallback) {
		return value != null && !value.isBlank() ? value.trim() : fallback;
	}
}
