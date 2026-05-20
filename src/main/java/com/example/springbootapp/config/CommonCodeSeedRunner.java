package com.example.springbootapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import com.example.springbootapp.mapper.CommonCodeMapper;

import javax.sql.DataSource;

/**
 * 대시보드·코드관리용 기본 코드가 없으면 seed SQL을 1회 적용합니다.
 */
@Profile("!test")
@Component
public class CommonCodeSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CommonCodeSeedRunner.class);
	private static final String COMBO_SEED_MARKER = "BULK_ACTION";

	private final DataSource dataSource;
	private final CommonCodeMapper commonCodeMapper;

	public CommonCodeSeedRunner(DataSource dataSource, CommonCodeMapper commonCodeMapper) {
		this.dataSource = dataSource;
		this.commonCodeMapper = commonCodeMapper;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			var populator = new ResourceDatabasePopulator();
			populator.setContinueOnError(true);
			populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
			if (commonCodeMapper.findByCodeId("DASHBOARD_PROJECT_TIME") == null) {
				populator.addScript(new ClassPathResource("schema/common_code_seed.sql"));
			}
			if (commonCodeMapper.findByCodeId(COMBO_SEED_MARKER) == null) {
				populator.addScript(new ClassPathResource("schema/common_code_combo_seed.sql"));
			}
			populator.execute(dataSource);
			log.info("공통코드 시드 적용 완료 (콤보박스용)");
		} catch (Exception ex) {
			log.warn("공통코드 시드 자동 적용 실패 — 수동 실행 또는 PK 설정 확인: {}", ex.getMessage());
		}
	}
}
