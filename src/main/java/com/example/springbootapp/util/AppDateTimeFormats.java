package com.example.springbootapp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 애플리케이션 공통 날짜·시간 표시·파싱 형식.
 * <ul>
 *   <li>날짜: {@value #DATE}</li>
 *   <li>일시: {@value #DATETIME} (24시간)</li>
 * </ul>
 */
public final class AppDateTimeFormats {

	public static final String DATE = "yyyy-MM-dd";
	public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE);
	public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME);

	private AppDateTimeFormats() {
	}

	public static String formatDate(LocalDate value) {
		return value != null ? value.format(DATE_FORMATTER) : null;
	}

	public static String formatDateTime(LocalDateTime value) {
		return value != null ? value.format(DATETIME_FORMATTER) : null;
	}

	public static LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trimmed = value.trim();
		try {
			return LocalDate.parse(trimmed, DATE_FORMATTER);
		} catch (DateTimeParseException ex) {
			return LocalDate.parse(trimmed);
		}
	}

	public static LocalDateTime parseDateTime(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trimmed = value.trim();
		try {
			return LocalDateTime.parse(trimmed, DATETIME_FORMATTER);
		} catch (DateTimeParseException ex) {
			if (trimmed.length() == 10) {
				return LocalDate.parse(trimmed, DATE_FORMATTER).atStartOfDay();
			}
			String normalized = trimmed.contains("T") ? trimmed : trimmed.replace(' ', 'T');
			if (normalized.length() == 16) {
				return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
			}
			return LocalDateTime.parse(normalized);
		}
	}
}
