package com.example.springbootapp.dto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
/**
 * EC 상품 등록·수정 폼/API 전달 객체.
 * <p>in/out: 상품 저장·조회 API request/response body</p>
 * <ul>
 *   <li>{@code productId} — in/out: 상품 ID</li>
 *   <li>{@code productNm} — in/out: 상품명</li>
 *   <li>{@code categoryCd} — in/out: 카테고리 코드</li>
 *   <li>{@code price} — in/out: 판매가</li>
 *   <li>{@code stockQty} — in/out: 재고 수량</li>
 *   <li>{@code statusCd} — in/out: 판매 상태 코드</li>
 *   <li>{@code imgUrl} — in/out: 대표 이미지 URL (레거시 단일 필드)</li>
 *   <li>{@code mainImageUrl} — in/out: 대표 이미지 URL ({@code imageUrls} 중 하나)</li>
 *   <li>{@code imageUrls} — in/out: 상품 이미지 URL 목록 (최대 {@value #MAX_IMAGES}개)</li>
 *   <li>{@code description} — in/out: 상품 설명</li>
 * </ul>
 */
public class EcmProductFormDto {
	public static final int MAX_IMAGES = 5;
	private Long productId;
	private String productNm;
	private String categoryCd;
	private BigDecimal price;
	private Integer stockQty;
	private String statusCd;
	private String imgUrl;
	private String mainImageUrl;
	private List<String> imageUrls = new ArrayList<>();
	private String description;
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getProductNm() {
		return productNm;
	}
	public void setProductNm(String productNm) {
		this.productNm = productNm;
	}
	public String getCategoryCd() {
		return categoryCd;
	}
	public void setCategoryCd(String categoryCd) {
		this.categoryCd = categoryCd;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public Integer getStockQty() {
		return stockQty;
	}
	public void setStockQty(Integer stockQty) {
		this.stockQty = stockQty;
	}
	public String getStatusCd() {
		return statusCd;
	}
	public void setStatusCd(String statusCd) {
		this.statusCd = statusCd;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getMainImageUrl() {
		return mainImageUrl;
	}
	public void setMainImageUrl(String mainImageUrl) {
		this.mainImageUrl = mainImageUrl;
	}
	public List<String> getImageUrls() {
		return imageUrls;
	}
	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "EcmProductFormDto{productId=" + productId + ", productNm='" + productNm + "', categoryCd='"
				+ categoryCd + "', price=" + price + ", stockQty=" + stockQty + ", statusCd='" + statusCd
				+ "', imgUrl='" + imgUrl + "', mainImageUrl='" + mainImageUrl + "', imageUrls=" + imageUrls
				+ ", description='" + description + "'}";
	}
}
