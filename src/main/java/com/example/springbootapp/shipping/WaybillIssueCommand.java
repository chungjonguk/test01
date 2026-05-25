package com.example.springbootapp.shipping;

import java.math.BigDecimal;

/**
 * 운송사 API에 전달할 접수 정보.
 */
public class WaybillIssueCommand {
	private String orderNo;
	private CarrierCd carrierCd;
	private String senderNm;
	private String senderPhone;
	private String senderZipcode;
	private String senderAddress;
	private String recipientNm;
	private String recipientPhone;
	private String zipcode;
	private String address;
	private String addressDetail;
	private int boxCnt;
	private BigDecimal weightKg;
	private String goodsNm;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public CarrierCd getCarrierCd() {
		return carrierCd;
	}

	public void setCarrierCd(CarrierCd carrierCd) {
		this.carrierCd = carrierCd;
	}

	public String getSenderNm() {
		return senderNm;
	}

	public void setSenderNm(String senderNm) {
		this.senderNm = senderNm;
	}

	public String getSenderPhone() {
		return senderPhone;
	}

	public void setSenderPhone(String senderPhone) {
		this.senderPhone = senderPhone;
	}

	public String getSenderZipcode() {
		return senderZipcode;
	}

	public void setSenderZipcode(String senderZipcode) {
		this.senderZipcode = senderZipcode;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public String getRecipientNm() {
		return recipientNm;
	}

	public void setRecipientNm(String recipientNm) {
		this.recipientNm = recipientNm;
	}

	public String getRecipientPhone() {
		return recipientPhone;
	}

	public void setRecipientPhone(String recipientPhone) {
		this.recipientPhone = recipientPhone;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	public int getBoxCnt() {
		return boxCnt;
	}

	public void setBoxCnt(int boxCnt) {
		this.boxCnt = boxCnt;
	}

	public BigDecimal getWeightKg() {
		return weightKg;
	}

	public void setWeightKg(BigDecimal weightKg) {
		this.weightKg = weightKg;
	}

	public String getGoodsNm() {
		return goodsNm;
	}

	public void setGoodsNm(String goodsNm) {
		this.goodsNm = goodsNm;
	}
}
