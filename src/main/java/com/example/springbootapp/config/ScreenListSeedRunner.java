package com.example.springbootapp.config;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import com.example.springbootapp.mapper.ScreenListMapper;
import com.example.springbootapp.service.ScreenListService;

/**
 * screen_list Java 카탈로그 등록 및 중복 URI 정리.
 * <p>DDL·메뉴 시드 SQL은 {@link DatabaseSchemaBootstrapRunner}에서 실행합니다.</p>
 */
@Profile("!test")
@Component
@Order(20)
public class ScreenListSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ScreenListSeedRunner.class);

	private final DataSource dataSource;
	private final ScreenListMapper screenListMapper;
	private final ScreenListService screenListService;

	public ScreenListSeedRunner(
			DataSource dataSource,
			ScreenListMapper screenListMapper,
			ScreenListService screenListService) {
		this.dataSource = dataSource;
		this.screenListMapper = screenListMapper;
		this.screenListService = screenListService;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			var catalog = ScreenCatalog.all();
			int saved = 0;
			for (var screen : catalog) {
				screenListService.save(screen, "SYSTEM");
				saved++;
			}

			var populator = new ResourceDatabasePopulator();
			populator.setContinueOnError(true);
			populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
			populator.addScript(new ClassPathResource("schema/screen_list_menu_dedup.sql"));
			populator.execute(dataSource);

			log.info("화면 목록(screen_list) 시드·한글 복구 완료 — catalog {}건, 저장 {}건, DB {}건",
					catalog.size(), saved, screenListMapper.countAll());
		} catch (Exception ex) {
			log.warn("screen_list 시드 적용 실패: {}", ex.getMessage());
		}
	}
}
