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
	private String profileImageUrl;
	private String coverImageUrl;
	private String homeZipcode;
	private String homeAddress;
	private String homeAddressDetail;
	private String homePhone;
	private String workZipcode;
	private String workAddress;
	private String workAddressDetail;
	private String workPhone;
	private String workCompanyName;
	private String primaryAddressType;
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
	public String getProfileImageUrl() {
		return profileImageUrl;
	}
	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}
	public String getCoverImageUrl() {
		return coverImageUrl;
	}
	public void setCoverImageUrl(String coverImageUrl) {
		this.coverImageUrl = coverImageUrl;
	}
	public String getHomeZipcode() {
		return homeZipcode;
	}
	public void setHomeZipcode(String homeZipcode) {
		this.homeZipcode = homeZipcode;
	}
	public String getHomeAddress() {
		return homeAddress;
	}
	public void setHomeAddress(String homeAddress) {
		this.homeAddress = homeAddress;
	}
	public String getHomeAddressDetail() {
		return homeAddressDetail;
	}
	public void setHomeAddressDetail(String homeAddressDetail) {
		this.homeAddressDetail = homeAddressDetail;
	}
	public String getHomePhone() {
		return homePhone;
	}
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}
	public String getWorkZipcode() {
		return workZipcode;
	}
	public void setWorkZipcode(String workZipcode) {
		this.workZipcode = workZipcode;
	}
	public String getWorkAddress() {
		return workAddress;
	}
	public void setWorkAddress(String workAddress) {
		this.workAddress = workAddress;
	}
	public String getWorkAddressDetail() {
		return workAddressDetail;
	}
	public void setWorkAddressDetail(String workAddressDetail) {
		this.workAddressDetail = workAddressDetail;
	}
	public String getWorkPhone() {
		return workPhone;
	}
	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}
	public String getWorkCompanyName() {
		return workCompanyName;
	}
	public void setWorkCompanyName(String workCompanyName) {
		this.workCompanyName = workCompanyName;
	}
	public String getPrimaryAddressType() {
		return primaryAddressType;
	}
	public void setPrimaryAddressType(String primaryAddressType) {
		this.primaryAddressType = primaryAddressType;
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
