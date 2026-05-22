package com.example.springbootapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 대시보드·코드관리용 기본 코드가 없으면 seed SQL을 1회 적용합니다.
 */
@Profile("!test")
@Component
@Order(10)
public class CommonCodeSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CommonCodeSeedRunner.class);

	private final DataSource dataSource;

	public CommonCodeSeedRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			var populator = new ResourceDatabasePopulator();
			populator.setContinueOnError(true);
			populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
			populator.addScript(new ClassPathResource("schema/common_code_charset.sql"));
			populator.addScript(new ClassPathResource("schema/common_code_seed.sql"));
			populator.addScript(new ClassPathResource("schema/common_code_combo_seed.sql"));
			populator.execute(dataSource);
			log.info("공통코드 시드·한글(utf8mb4) 갱신 완료");
		} catch (Exception ex) {
			log.warn("공통코드 시드 자동 적용 실패 — 수동 실행 또는 PK 설정 확인: {}", ex.getMessage());
		}
	}
}
