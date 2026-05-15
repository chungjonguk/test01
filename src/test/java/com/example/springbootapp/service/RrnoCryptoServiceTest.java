package com.example.springbootapp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RrnoCryptoServiceTest {

	@Autowired
	private RrnoCryptoService rrnoCryptoService;

	@Test
	void encryptAndDecrypt_roundTrip() {
		String plain = "900101-1234567";
		String encrypted = rrnoCryptoService.encrypt(plain);

		assertThat(encrypted).startsWith("ENC:");
		assertThat(encrypted).isNotEqualTo(plain);

		String decrypted = rrnoCryptoService.decrypt(encrypted);
		assertThat(decrypted).isEqualTo(plain);
	}
}
