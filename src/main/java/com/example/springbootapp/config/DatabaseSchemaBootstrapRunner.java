package com.example.springbootapp.config;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * 서버 기동 시 {@link SchemaScriptCatalog}에 정의된 DDL·시드 SQL을 일괄 적용합니다.
 */
@Profile("!test")
@Component
@Order(3)
public class DatabaseSchemaBootstrapRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaBootstrapRunner.class);

	private final DataSource dataSource;

	@Value("${app.schema.auto-apply:true}")
	private boolean autoApplyEnabled;

	public DatabaseSchemaBootstrapRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!autoApplyEnabled) {
			log.info("DB 스키마 자동 적용 비활성(app.schema.auto-apply=false)");
			return;
		}
		try {
			var populator = new ResourceDatabasePopulator();
			populator.setContinueOnError(true);
			populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
			int scriptCount = 0;
			for (String path : SchemaScriptCatalog.startupScripts()) {
				populator.addScript(new ClassPathResource(path));
				scriptCount++;
			}
			populator.execute(dataSource);
			log.info("DB 스키마·시드 SQL 적용 완료 — {}개 스크립트", scriptCount);
		} catch (Exception ex) {
			log.warn("DB 스키마·시드 SQL 적용 실패: {}", ex.getMessage());
		}
	}
}
