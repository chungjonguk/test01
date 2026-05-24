package com.example.springbootapp.dto;
/**
 * 거래처(회사) 등록·수정 폼/API 전달 객체.
 * <p>in/out: 거래처 저장·조회 API request/response body</p>
 * <ul>
 *   <li>{@code companyId} — in/out: 거래처 ID</li>
 *   <li>{@code companyNm} — in/out: 회사명</li>
 *   <li>{@code bizNo} — in/out: 사업자번호</li>
 *   <li>{@code ceoNm} — in/out: 대표자명</li>
 *   <li>{@code tel} — in/out: 전화번호</li>
 *   <li>{@code email} — in/out: 이메일</li>
 *   <li>{@code address} — in/out: 주소</li>
 *   <li>{@code statusCd} — in/out: 상태 코드</li>
 *   <li>{@code useYn} — in/out: 사용 여부</li>
 *   <li>{@code memo} — in/out: 메모</li>
 * </ul>
 */
public class BizCompanyFormDto {
	private Long companyId;
	private String companyNm;
	private String bizNo;
	private String ceoNm;
	private String tel;
	private String email;
	private String address;
	private String statusCd;
	private String useYn;
	private String memo;
	public Long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	public String getCompanyNm() {
		return companyNm;
	}
	public void setCompanyNm(String companyNm) {
		this.companyNm = companyNm;
	}
	public String getBizNo() {
		return bizNo;
	}
	public void setBizNo(String bizNo) {
		this.bizNo = bizNo;
	}
	public String getCeoNm() {
		return ceoNm;
	}
	public void setCeoNm(String ceoNm) {
		this.ceoNm = ceoNm;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getStatusCd() {
		return statusCd;
	}
	public void setStatusCd(String statusCd) {
		this.statusCd = statusCd;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
}
