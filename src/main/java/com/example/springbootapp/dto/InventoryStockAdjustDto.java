package com.example.springbootapp.dto;

/**
 * 재고 조정 요청 — {@code SET}(직접설정), {@code ADD}(입고), {@code SUB}(출고).
 */
public class InventoryStockAdjustDto {

	private String adjustType;
	private Integer quantity;

	public String getAdjustType() {
		return adjustType;
	}

	public void setAdjustType(String adjustType) {
		this.adjustType = adjustType;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}
