package com.example.springbootapp.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.springbootapp.util.AppDateTimeFormats;

/**
 * Thymeleaf·화면용 날짜 포맷 헬퍼 ({@code appDates}).
 */
@ControllerAdvice
public class DateTimeViewAdvice {

	@ModelAttribute("appDates")
	public AppDates appDates() {
		return AppDates.INSTANCE;
	}

	@ModelAttribute("appDatePattern")
	public String appDatePattern() {
		return AppDateTimeFormats.DATE;
	}

	@ModelAttribute("appDateTimePattern")
	public String appDateTimePattern() {
		return AppDateTimeFormats.DATETIME;
	}

	public static final class AppDates {
		public static final AppDates INSTANCE = new AppDates();

		private AppDates() {
		}

		public String date(LocalDate value) {
			String formatted = AppDateTimeFormats.formatDate(value);
			return formatted != null ? formatted : "-";
		}

		public String dateTime(LocalDateTime value) {
			String formatted = AppDateTimeFormats.formatDateTime(value);
			return formatted != null ? formatted : "-";
		}
	}
}
