package com.example.springbootapp.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordPolicyValidatorTest {

	@Test
	void acceptsPasswordWithLetterDigitAndSpecial() {
		assertTrue(PasswordPolicyValidator.isValid("abc123!"));
		assertTrue(PasswordPolicyValidator.isValid("Pass@word1"));
	}

	@Test
	void rejectsPasswordMissingRequiredCharacterTypes() {
		assertFalse(PasswordPolicyValidator.isValid("abcdefgh"));
		assertFalse(PasswordPolicyValidator.isValid("12345678"));
		assertFalse(PasswordPolicyValidator.isValid("!@#$%^&*"));
		assertFalse(PasswordPolicyValidator.isValid("abc123"));
		assertFalse(PasswordPolicyValidator.isValid(null));
		assertFalse(PasswordPolicyValidator.isValid(""));
	}
}
