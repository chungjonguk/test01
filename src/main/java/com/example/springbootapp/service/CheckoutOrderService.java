package com.example.springbootapp.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.domain.EcmOrder;
import com.example.springbootapp.domain.EcmPayment;
import com.example.springbootapp.mapper.EcmOrderMapper;
import com.example.springbootapp.mapper.EcmPaymentMapper;

@Service
public class CheckoutOrderService {

	public static final String ORDER_PENDING = "PROCESSING";
	public static final String ORDER_PAID = "COMPLETED";
	public static final String ORDER_FAILED = "On Hold";

	private final EcmOrderMapper ecmOrderMapper;
	private final EcmPaymentMapper ecmPaymentMapper;

	public CheckoutOrderService(EcmOrderMapper ecmOrderMapper, EcmPaymentMapper ecmPaymentMapper) {
		this.ecmOrderMapper = ecmOrderMapper;
		this.ecmPaymentMapper = ecmPaymentMapper;
	}

	@Transactional
	public Long createPendingOrder(String orderNo, BigDecimal amount, Long customerId, String shipTo, String actor) {
		EcmOrder existing = ecmOrderMapper.findByOrderNo(orderNo);
		if (existing != null) {
			return existing.getOrderId();
		}
		EcmOrder order = new EcmOrder();
		order.setOrderNo(orderNo);
		order.setCustomerId(customerId != null ? customerId : 1L);
		order.setOrderDt(LocalDate.now());
		order.setShipTo(shipTo != null && !shipTo.isBlank() ? shipTo : "배송지 미입력");
		order.setShippingMethod("Standard");
		order.setStatusCd(ORDER_PENDING);
		order.setAmount(amount);
		order.setRegId(actor);
		order.setUpdateId(actor);
		ecmOrderMapper.insert(order);
		linkPaymentToOrder(orderNo, order.getOrderId(), actor);
		return order.getOrderId();
	}

	@Transactional
	public void markOrderPaid(String orderNo, String actor) {
		ecmOrderMapper.updateStatusByOrderNo(orderNo, ORDER_PAID, actor);
	}

	@Transactional
	public void markOrderFailed(String orderNo, String actor) {
		ecmOrderMapper.updateStatusByOrderNo(orderNo, ORDER_FAILED, actor);
	}

	private void linkPaymentToOrder(String orderNo, Long orderId, String actor) {
		EcmPayment payment = ecmPaymentMapper.findByOrderNo(orderNo);
		if (payment == null || orderId == null) {
			return;
		}
		payment.setOrderId(orderId);
		payment.setUpdateId(actor);
		ecmPaymentMapper.updateOrderId(payment);
	}
}
