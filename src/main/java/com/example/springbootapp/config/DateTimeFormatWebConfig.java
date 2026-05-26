package com.example.springbootapp.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.example.springbootapp.util.AppDateTimeFormats;

/**
 * 요청 파라미터·폼 바인딩용 날짜·시간 변환.
 */
@Configuration
public class DateTimeFormatWebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToLocalDateConverter());
		registry.addConverter(new StringToLocalDateTimeConverter());
	}

	private static final class StringToLocalDateConverter implements Converter<String, LocalDate> {
		@Override
		public LocalDate convert(String source) {
			return AppDateTimeFormats.parseDate(source);
		}
	}

	private static final class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
		@Override
		public LocalDateTime convert(String source) {
			return AppDateTimeFormats.parseDateTime(source);
		}
	}
}
