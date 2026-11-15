package com.example.springbootapp.config;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.jdbc.datasource.init.ScriptUtils;
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

		try (Connection connection = dataSource.getConnection()) {
			var meta = connection.getMetaData();
			log.info("스키마 스크립트 적용 시작 — url={}, user={}, product={}",
					meta.getURL(), meta.getUserName(), meta.getDatabaseProductName());
		} catch (Exception ex) {
			log.error("스키마 적용 전 DB 접속 실패: {}", ex.getMessage());
			throw new IllegalStateException("스키마 적용을 위한 DB 접속에 실패했습니다.", ex);
		}

		int applied = 0;
		int skipped = 0;
		List<String> failed = new ArrayList<>();

		for (String path : SchemaScriptCatalog.startupScripts()) {
			ClassPathResource resource = new ClassPathResource(path);
			if (!resource.exists()) {
				skipped++;
				log.warn("스키마 스크립트 없음(건너뜀): {}", path);
				continue;
			}
			try {
				var populator = new ResourceDatabasePopulator();
				populator.setContinueOnError(true);
				populator.setIgnoreFailedDrops(true);
				populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
				populator.setSeparator(ScriptUtils.DEFAULT_STATEMENT_SEPARATOR);
				populator.addScript(resource);
				populator.execute(dataSource);
				applied++;
				log.debug("스키마 스크립트 적용: {}", path);
			} catch (Exception ex) {
				failed.add(path);
				log.warn("스키마 스크립트 적용 실패(건너뜀): {} — {}", path, ex.getMessage());
			}
		}

		int tableCount = countPublicTables();
		log.info("DB 스키마·시드 SQL 적용 완료 — 성공 {}개 / 없음 {}개 / 실패 {}개 / public 테이블 {}개",
				applied, skipped, failed.size(), tableCount);
		if (!failed.isEmpty()) {
			log.warn("실패한 스키마 스크립트: {}", String.join(", ", failed));
		}
		if (tableCount == 0) {
			throw new IllegalStateException(
					"스키마 적용 후에도 public 테이블이 없습니다. SchemaScriptCatalog·SQL·DB 권한을 확인하세요.");
		}
	}

	private int countPublicTables() {
		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery(
						"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'")) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception ex) {
			log.warn("테이블 수 조회 실패: {}", ex.getMessage());
		}
		return -1;
	}
}
