package com.example.springbootapp.dto;
/**
 * 회원가입 폼/API 전달 객체.
 * <p>in: 회원가입 요청 body</p>
 * <ul>
 *   <li>{@code id} — in: 로그인 ID</li>
 *   <li>{@code pw} — in: 비밀번호</li>
 *   <li>{@code name} — in: 이름</li>
 *   <li>{@code sex} — in: 성별 코드</li>
 *   <li>{@code rrno} — in: 주민등록번호</li>
 *   <li>{@code email} — in: 이메일</li>
 *   <li>{@code zipcode} — in: 우편번호</li>
 *   <li>{@code address} — in: 기본 주소</li>
 *   <li>{@code addressDetail} — in: 상세 주소</li>
 * </ul>
 */
public class UserRegisterDto {
	private String id;
	private String pw;
	private String name;
	private String sex;
	private String rrno;
	private String email;
	private String zipcode;
	private String address;
	private String addressDetail;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPw() {
		return pw;
	}
	public void setPw(String pw) {
		this.pw = pw;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getRrno() {
		return rrno;
	}
	public void setRrno(String rrno) {
		this.rrno = rrno;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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
	@Override
	public String toString() {
		return "UserRegisterDto{id='" + id + "', pw='***', name='" + name + "', sex='" + sex + "', rrno='***', email='"
				+ email + "', zipcode='" + zipcode + "', address='" + address + "', addressDetail='" + addressDetail
				+ "'}";
	}
}
