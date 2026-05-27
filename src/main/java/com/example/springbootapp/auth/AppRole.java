package com.example.springbootapp.auth;

/**
 * 애플리케이션 권한 역할.
 */
public enum AppRole {
	/** 전체 화면·API 수정 */
	PLATFORM_ADMIN,
	/** 업체·쇼핑몰 운영 메뉴만 */
	COMPANY_ADMIN,
	/** 업체가 허용한 고객 메뉴만 (주로 조회) */
	COMPANY_CUSTOMER;

	public static AppRole fromCode(String code) {
		if (code == null || code.isBlank()) {
			return null;
		}
		try {
			return AppRole.valueOf(code.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	public boolean canWriteGlobally() {
		return this == PLATFORM_ADMIN;
	}
}
