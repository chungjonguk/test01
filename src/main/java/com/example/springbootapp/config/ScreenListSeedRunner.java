package com.example.springbootapp.config;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import com.example.springbootapp.mapper.ScreenListMapper;
import com.example.springbootapp.service.ScreenListService;
import javax.sql.DataSource;
/**
 * screen_list DDL 적용 후 Java 시드로 화면명(한글)을 등록·갱신합니다.
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
			var populator = new ResourceDatabasePopulator();
			populator.setContinueOnError(true);
			populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
			populator.addScript(new ClassPathResource("schema/screen_list.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_alter.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_charset.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_admin_companies_menu.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_admin_company_section.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_admin_company_page_images.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_admin_inventory_menu.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_admin_shipping_menu.sql"));
			populator.addScript(new ClassPathResource("schema/screen_list_shopping_mall_menu.sql"));
			populator.execute(dataSource);
			var catalog = ScreenCatalog.all();
			int saved = 0;
			for (var screen : catalog) {
				screenListService.save(screen, "SYSTEM");
				saved++;
			}
			log.info("화면 목록(screen_list) 시드·한글 복구 완료 — catalog {}건, 저장 {}건, DB {}건",
					catalog.size(), saved, screenListMapper.countAll());
		} catch (Exception ex) {
			log.warn("screen_list 시드 적용 실패: {}", ex.getMessage());
		}
	}
}
