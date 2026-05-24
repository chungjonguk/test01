package com.example.springbootapp.service;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.domain.EcmOrder;
import com.example.springbootapp.domain.EcmPayment;
import com.example.springbootapp.mapper.EcmOrderMapper;
import com.example.springbootapp.mapper.EcmPaymentMapper;
/**
 * 체크아웃 주문 생성 및 결제 결과에 따른 주문 상태 변경을 처리하는 서비스.
 */
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
	/**
	 * 결제 대기 상태의 주문을 생성한다. 동일 주문번호가 있으면 기존 ID를 반환한다.
	 *
	 * @param orderNo    주문번호
	 * @param amount     주문 금액
	 * @param customerId 고객 ID (null이면 기본값 1)
	 * @param shipTo     배송지
	 * @param actor      등록·수정자 ID
	 * @return 생성(또는 기존) 주문 ID
	 */
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
	/**
	 * 주문 상태를 결제 완료로 변경한다.
	 *
	 * @param orderNo 주문번호
	 * @param actor   수정자 ID
	 */
	@Transactional
	public void markOrderPaid(String orderNo, String actor) {
		ecmOrderMapper.updateStatusByOrderNo(orderNo, ORDER_PAID, actor);
	}
	/**
	 * 주문 상태를 결제 실패(보류)로 변경한다.
	 *
	 * @param orderNo 주문번호
	 * @param actor   수정자 ID
	 */
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
