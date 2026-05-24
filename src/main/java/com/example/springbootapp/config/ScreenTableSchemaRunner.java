package com.example.springbootapp.config;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 공통코드·업무·화면-테이블 매핑 DDL 및 샘플 데이터 적용.
 */
@Profile("!test")
@Component
@Order(5)
public class ScreenTableSchemaRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ScreenTableSchemaRunner.class);

	private final DataSource dataSource;

	public ScreenTableSchemaRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			var populator = new ResourceDatabasePopulator();
			populator.setContinueOnError(true);
			populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
			populator.addScript(new ClassPathResource("schema/common_code.sql"));
			populator.addScript(new ClassPathResource("schema/screen_table_map.sql"));
			populator.addScript(new ClassPathResource("schema/user_access_log.sql"));
			populator.addScript(new ClassPathResource("schema/user_access_log_alter.sql"));
			populator.addScript(new ClassPathResource("schema/biz_company.sql"));
			populator.addScript(new ClassPathResource("schema/biz_company_seed.sql"));
			populator.addScript(new ClassPathResource("schema/ecm_payment.sql"));
			populator.addScript(new ClassPathResource("schema/tables/biz_schema.sql"));
			populator.addScript(new ClassPathResource("schema/tables/ecm_product_image.sql"));
			populator.addScript(new ClassPathResource("schema/tables/biz_seed.sql"));
			populator.execute(dataSource);
			log.info("화면별 업무 테이블 DDL·샘플 데이터 적용 완료");
		} catch (Exception ex) {
			log.warn("업무 테이블 스키마 적용 실패: {}", ex.getMessage());
		}
	}
}
