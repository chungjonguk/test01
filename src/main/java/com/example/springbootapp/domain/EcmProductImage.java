package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class EcmProductImage {
	private Long imageId;
	private Long productId;
	private int sortOrd;
	private String imgUrl;
	private String regId;
	private LocalDateTime regDt;
	public Long getImageId() {
		return imageId;
	}
	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public int getSortOrd() {
		return sortOrd;
	}
	public void setSortOrd(int sortOrd) {
		this.sortOrd = sortOrd;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getRegId() {
		return regId;
	}
	public void setRegId(String regId) {
		this.regId = regId;
	}
	public LocalDateTime getRegDt() {
		return regDt;
	}
	public void setRegDt(LocalDateTime regDt) {
		this.regDt = regDt;
	}
}
