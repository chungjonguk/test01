package com.example.springbootapp.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.example.springbootapp.mapper.ScreenListMapper;
import com.example.springbootapp.service.ScreenTableMapService;
/**
 * screen_list 전체 URI에 대해 screen_table_map(화면↔테이블) 매핑을 갱신합니다.
 */
@Profile("!test")
@Component
@Order(30)
public class ScreenTableMapSeedRunner implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(ScreenTableMapSeedRunner.class);
	private final ScreenListMapper screenListMapper;
	private final ScreenTableMapService screenTableMapService;
	public ScreenTableMapSeedRunner(
			ScreenListMapper screenListMapper,
			ScreenTableMapService screenTableMapService) {
		this.screenListMapper = screenListMapper;
		this.screenTableMapService = screenTableMapService;
	}
	@Override
	public void run(ApplicationArguments args) {
		try {
			var screens = screenListMapper.findForAdmin(null, null, null, null);
			screenTableMapService.syncFromScreens(screens, "SYSTEM");
			log.info("화면별 테이블 매핑(screen_table_map) 갱신 완료 — {}건", screens.size());
		} catch (Exception ex) {
			log.warn("screen_table_map 시드 실패: {}", ex.getMessage());
		}
	}
}
