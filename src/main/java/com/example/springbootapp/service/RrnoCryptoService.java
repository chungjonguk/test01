package com.example.springbootapp.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RrnoCryptoService {

	private static final String PREFIX = "ENC:";
	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_LENGTH = 128;

	private final SecretKey secretKey;

	public RrnoCryptoService(@Value("${app.crypto.rrno.secret-key}") String secretKeyRaw) {
		byte[] keyBytes = secretKeyRaw.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length != 32) {
			throw new IllegalArgumentException("app.crypto.rrno.secret-key는 32바이트(문자)여야 합니다.");
		}
		this.secretKey = new SecretKeySpec(keyBytes, "AES");
	}

	public String encrypt(String plainText) {
		if (plainText == null || plainText.isBlank()) {
			return plainText;
		}
		if (plainText.startsWith(PREFIX)) {
			return plainText;
		}
		try {
			byte[] iv = new byte[GCM_IV_LENGTH];
			SecureRandom.getInstanceStrong().nextBytes(iv);

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

			byte[] combined = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

			return PREFIX + Base64.getEncoder().encodeToString(combined);
		} catch (Exception ex) {
			throw new IllegalStateException("주민번호 암호화에 실패했습니다.", ex);
		}
	}

	public String decrypt(String cipherText) {
		if (cipherText == null || cipherText.isBlank()) {
			return cipherText;
		}
		if (!cipherText.startsWith(PREFIX)) {
			return cipherText;
		}
		try {
			byte[] combined = Base64.getDecoder().decode(cipherText.substring(PREFIX.length()));
			byte[] iv = new byte[GCM_IV_LENGTH];
			byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
			System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
			System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			byte[] decrypted = cipher.doFinal(encrypted);

			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception ex) {
			throw new IllegalStateException("주민번호 복호화에 실패했습니다.", ex);
		}
	}
}
