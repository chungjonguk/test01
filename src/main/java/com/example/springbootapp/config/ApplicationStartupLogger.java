package com.example.springbootapp.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class ApplicationStartupLogger implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(ApplicationStartupLogger.class);

	private final DataSource dataSource;

	public ApplicationStartupLogger(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
		if (!(event.getApplicationContext() instanceof WebServerApplicationContext webCtx)) {
			return;
		}
		int port = webCtx.getWebServer().getPort();
		String name = event.getApplicationContext().getEnvironment().getProperty("spring.application.name", "app");
		try (var connection = dataSource.getConnection()) {
			log.info("[{}] 서비스 준비 완료 — DB url={}", name, connection.getMetaData().getURL());
		} catch (Exception ex) {
			log.warn("[{}] 서비스 준비됨 — DB 재확인 실패(이미 기동 검사 통과 후 연결 끊김 가능)", name, ex);
		}
		log.info("[{}] 서버 포트 {} — 예: http://localhost:{}/dashboard", name, port, port);
	}
}
