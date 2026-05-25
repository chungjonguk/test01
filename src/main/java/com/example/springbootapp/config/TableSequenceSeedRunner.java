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

import com.example.springbootapp.service.TableRandomIdService;
import com.example.springbootapp.service.TableSequenceService;

import javax.sql.DataSource;

/**
 * 테이블별 시퀀스·난수 ID 마스터 DDL 적용 및 초기 등록.
 */
@Profile("!test")
@Component
@Order(4)
public class TableSequenceSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(TableSequenceSeedRunner.class);

	private final DataSource dataSource;
	private final TableSequenceService tableSequenceService;
	private final TableRandomIdService tableRandomIdService;

	public TableSequenceSeedRunner(
			DataSource dataSource,
			TableSequenceService tableSequenceService,
			TableRandomIdService tableRandomIdService) {
		this.dataSource = dataSource;
		this.tableSequenceService = tableSequenceService;
		this.tableRandomIdService = tableRandomIdService;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			var populator = new ResourceDatabasePopulator();
			populator.setContinueOnError(true);
			populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
			populator.addScript(new ClassPathResource("schema/sys_table_sequence.sql"));
			populator.addScript(new ClassPathResource("schema/sys_table_random_id.sql"));
			populator.execute(dataSource);

			int seqRegistered = tableSequenceService.registerAllFromCatalog();
			int seqSynced = tableSequenceService.syncNextValuesFromTables();
			int seqTotal = tableSequenceService.countAll();

			int randomRegistered = tableRandomIdService.registerAllFromCatalog();
			int randomTotal = tableRandomIdService.countAll();

			log.info("테이블별 시퀀스 준비 — 신규 {}건, MAX동기화 {}건, 전체 {}건",
					seqRegistered, seqSynced, seqTotal);
			log.info("테이블별 난수 ID 준비 — 신규 {}건, 전체 {}건", randomRegistered, randomTotal);
		} catch (Exception ex) {
			log.warn("테이블별 ID(시퀀스·난수) 초기화 실패: {}", ex.getMessage());
		}
	}
}
