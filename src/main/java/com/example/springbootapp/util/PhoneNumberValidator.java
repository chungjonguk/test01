package com.example.springbootapp.util;

import java.util.regex.Pattern;

/**
 * 한국 전화번호 형식 검증·정규화 (자택/직장 연락처 등).
 */
public final class PhoneNumberValidator {

	private static final Pattern FORMAT_PATTERN = Pattern.compile(
			"^(01[0-9]-\\d{3,4}-\\d{4}|0\\d{1,2}-\\d{3,4}-\\d{4})$");

	private PhoneNumberValidator() {
	}

	/**
	 * 숫자만 추출합니다.
	 */
	public static String digitsOnly(String value) {
		if (value == null) {
			return "";
		}
		return value.replaceAll("\\D", "");
	}

	/**
	 * 입력값을 표시용 하이픈 형식으로 정규화합니다. 유효하지 않으면 null.
	 */
	public static String normalize(String raw) {
		String digits = digitsOnly(raw);
		if (digits.isEmpty()) {
			return null;
		}
		if (digits.startsWith("02")) {
			if (digits.length() == 9) {
				return digits.substring(0, 2) + "-" + digits.substring(2, 5) + "-" + digits.substring(5);
			}
			if (digits.length() == 10) {
				return digits.substring(0, 2) + "-" + digits.substring(2, 6) + "-" + digits.substring(6);
			}
			return null;
		}
		if (digits.startsWith("01")) {
			if (digits.length() == 10) {
				return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
			}
			if (digits.length() == 11) {
				return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
			}
			return null;
		}
		if (digits.length() == 10 && digits.charAt(0) == '0') {
			return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
		}
		if (digits.length() == 11 && digits.charAt(0) == '0') {
			return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
		}
		return null;
	}

	public static boolean isValid(String value) {
		if (value == null || value.isBlank()) {
			return true;
		}
		String normalized = normalize(value);
		return normalized != null && FORMAT_PATTERN.matcher(normalized).matches();
	}

	/**
	 * 선택 입력 — 비어 있으면 null, 있으면 정규화 후 형식 검증.
	 */
	public static String normalizeOptional(String raw, String fieldLabel) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String normalized = normalize(raw);
		if (normalized == null) {
			throw new IllegalArgumentException(
					fieldLabel + " 형식이 올바르지 않습니다. 예: 010-1234-5678, 02-1234-5678");
		}
		return normalized;
	}
}
