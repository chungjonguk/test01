package com.example.springbootapp.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.springbootapp.util.AppDateTimeFormats;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * REST JSON 날짜·시간 직렬화 형식 (yyyy-MM-dd, yyyy-MM-dd HH:mm:ss).
 */
@Configuration
public class JacksonDateTimeConfig {

	@Bean
	Jackson2ObjectMapperBuilderCustomizer jacksonDateTimeCustomizer() {
		return builder -> {
			builder.simpleDateFormat(AppDateTimeFormats.DATETIME);
			builder.serializers(
					new LocalDateSerializer(AppDateTimeFormats.DATE_FORMATTER),
					new LocalDateTimeSerializer(AppDateTimeFormats.DATETIME_FORMATTER));
			builder.deserializers(
					new LocalDateDeserializer(AppDateTimeFormats.DATE_FORMATTER),
					new LocalDateTimeDeserializer(AppDateTimeFormats.DATETIME_FORMATTER));
		};
	}
}
