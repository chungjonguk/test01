package com.example.springbootapp.config;

import java.util.TimeZone;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/** JVM 기본 시간대를 한국(Asia/Seoul)으로 고정합니다. */
@Configuration
public class AppTimeZoneConfig {

	private static final String ZONE_ID = "Asia/Seoul";

	@PostConstruct
	void initDefaultTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(ZONE_ID));
	}
}
