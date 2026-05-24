package com.example.springbootapp.dto;
import java.math.BigDecimal;
/**
 * KG이니시스 결제 준비(Prepare) API 요청 객체.
 * <p>in: 결제창 호출 전 주문·구매자 정보</p>
 * <ul>
 *   <li>{@code amount} — in: 결제 금액</li>
 *   <li>{@code goodName} — in: 상품명</li>
 *   <li>{@code buyerName} — in: 구매자명</li>
 *   <li>{@code buyerTel} — in: 구매자 연락처</li>
 *   <li>{@code buyerEmail} — in: 구매자 이메일</li>
 *   <li>{@code orderId} — in: 주문 ID</li>
 *   <li>{@code customerId} — in: 고객 ID</li>
 *   <li>{@code shipTo} — in: 배송지</li>
 * </ul>
 */
public class InicisPrepareRequest {
	private BigDecimal amount;
	private String goodName;
	private String buyerName;
	private String buyerTel;
	private String buyerEmail;
	private Long orderId;
	private Long customerId;
	private String shipTo;
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getGoodName() {
		return goodName;
	}
	public void setGoodName(String goodName) {
		this.goodName = goodName;
	}
	public String getBuyerName() {
		return buyerName;
	}
	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}
	public String getBuyerTel() {
		return buyerTel;
	}
	public void setBuyerTel(String buyerTel) {
		this.buyerTel = buyerTel;
	}
	public String getBuyerEmail() {
		return buyerEmail;
	}
	public void setBuyerEmail(String buyerEmail) {
		this.buyerEmail = buyerEmail;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public String getShipTo() {
		return shipTo;
	}
	public void setShipTo(String shipTo) {
		this.shipTo = shipTo;
	}
	@Override
	public String toString() {
		return "InicisPrepareRequest{amount=" + amount + ", goodName='" + goodName + "', buyerName='" + buyerName
				+ "', buyerTel='" + buyerTel + "', buyerEmail='" + buyerEmail + "', orderId=" + orderId
				+ ", customerId=" + customerId + ", shipTo='" + shipTo + "'}";
	}
}
