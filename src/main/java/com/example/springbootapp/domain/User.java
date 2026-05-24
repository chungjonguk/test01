package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class User {
	private String id;
	private String pw;
	private String name;
	private String sex;
	private String rrno;
	private String email;
	private String zipcode;
	private String address;
	private String addressDetail;
	private String updateId;
	private LocalDateTime regDt;
	private LocalDateTime updDt;
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
	public String getUpdateId() {
		return updateId;
	}
	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}
	public LocalDateTime getRegDt() {
		return regDt;
	}
	public void setRegDt(LocalDateTime regDt) {
		this.regDt = regDt;
	}
	public LocalDateTime getUpdDt() {
		return updDt;
	}
	public void setUpdDt(LocalDateTime updDt) {
		this.updDt = updDt;
	}
	@Override
	public String toString() {
		return "User{id='" + id + "', pw='***', name='" + name + "', sex='" + sex + "', rrno='***', email='" + email
				+ "', zipcode='" + zipcode + "', address='" + address + "', addressDetail='" + addressDetail
				+ "', updateId='" + updateId + "', regDt=" + regDt + ", updDt=" + updDt + "}";
	}
}
