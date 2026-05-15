package com.example.springbootapp.util;

import java.util.regex.Pattern;

public final class PasswordPolicyValidator {

	private static final Pattern HAS_LETTER = Pattern.compile("[A-Za-z]");
	private static final Pattern HAS_DIGIT = Pattern.compile("\\d");
	private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]");

	private PasswordPolicyValidator() {
	}

	public static boolean isValid(String password) {
		if (password == null || password.isEmpty()) {
			return false;
		}
		return HAS_LETTER.matcher(password).find()
				&& HAS_DIGIT.matcher(password).find()
				&& HAS_SPECIAL.matcher(password).find();
	}

	public static String requirementMessage() {
		return "비밀번호는 영문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.";
	}
}
