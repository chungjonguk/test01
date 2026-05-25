package com.example.springbootapp.shipping;

import java.util.Locale;
import java.util.Optional;

/**
 * 택배사 코드 — CJ대한통운, 우체국, 롯데택배.
 */
public enum CarrierCd {
	CJ("CJ", "CJ대한통운"),
	EPOST("EPOST", "우체국"),
	LOTTE("LOTTE", "롯데택배");

	private final String code;
	private final String label;

	CarrierCd(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static Optional<CarrierCd> fromCode(String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		String normalized = raw.trim().toUpperCase(Locale.ROOT);
		for (CarrierCd cd : values()) {
			if (cd.code.equals(normalized)) {
				return Optional.of(cd);
			}
		}
		return Optional.empty();
	}
}
