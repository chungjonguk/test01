package com.example.springbootapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.springbootapp.service.TableRandomIdService;
import com.example.springbootapp.service.TableSequenceService;

/**
 * DDL 적용 후 시퀀스·난수 ID 마스터를 Java 카탈로그 기준으로 등록·동기화합니다.
 * <p>SQL은 {@link DatabaseSchemaBootstrapRunner}에서 실행합니다.</p>
 */
@Profile("!test")
@Component
@Order(4)
public class TableSequenceSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(TableSequenceSeedRunner.class);

	private final TableSequenceService tableSequenceService;
	private final TableRandomIdService tableRandomIdService;

	public TableSequenceSeedRunner(
			TableSequenceService tableSequenceService,
			TableRandomIdService tableRandomIdService) {
		this.tableSequenceService = tableSequenceService;
		this.tableRandomIdService = tableRandomIdService;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
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
