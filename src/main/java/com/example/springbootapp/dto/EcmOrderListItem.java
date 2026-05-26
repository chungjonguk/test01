package com.example.springbootapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 주문 목록 그리드 행 */
public class EcmOrderListItem {
	private Long orderId;
	private String orderNo;
	private String customerNm;
	private String customerEmail;
	private LocalDate orderDt;
	private String shipTo;
	private String statusCd;
	private BigDecimal amount;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getCustomerNm() {
		return customerNm;
	}

	public void setCustomerNm(String customerNm) {
		this.customerNm = customerNm;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public LocalDate getOrderDt() {
		return orderDt;
	}

	public void setOrderDt(LocalDate orderDt) {
		this.orderDt = orderDt;
	}

	public String getShipTo() {
		return shipTo;
	}

	public void setShipTo(String shipTo) {
		this.shipTo = shipTo;
	}

	public String getStatusCd() {
		return statusCd;
	}

	public void setStatusCd(String statusCd) {
		this.statusCd = statusCd;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}
