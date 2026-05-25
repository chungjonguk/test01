package com.example.springbootapp.dto;

import java.math.BigDecimal;

/**
 * 운송장 발급 요청.
 */
public class WaybillIssueRequestDto {
	private Long orderId;
	private String carrierCd;
	private Integer boxCnt;
	private BigDecimal weightKg;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getCarrierCd() {
		return carrierCd;
	}

	public void setCarrierCd(String carrierCd) {
		this.carrierCd = carrierCd;
	}

	public Integer getBoxCnt() {
		return boxCnt;
	}

	public void setBoxCnt(Integer boxCnt) {
		this.boxCnt = boxCnt;
	}

	public BigDecimal getWeightKg() {
		return weightKg;
	}

	public void setWeightKg(BigDecimal weightKg) {
		this.weightKg = weightKg;
	}
}
