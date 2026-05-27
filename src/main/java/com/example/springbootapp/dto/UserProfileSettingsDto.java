package com.example.springbootapp.dto;

/**
 * 사용자 설정 화면 — 프로필 기본 정보 저장 요청.
 */
public class UserProfileSettingsDto {

	private String name;
	private String email;
	private String homeZipcode;
	private String homeAddress;
	private String homeAddressDetail;
	private String homePhone;
	private String workZipcode;
	private String workAddress;
	private String workAddressDetail;
	private String workPhone;
	private String workCompanyName;
	/** 기본 주소 구분: HOME | WORK */
	private String primaryAddressType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
}
