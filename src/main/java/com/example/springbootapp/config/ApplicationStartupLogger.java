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
			log.info("[{}] MySQL 연결 성공 — url={}", name, connection.getMetaData().getURL());
		} catch (Exception ex) {
			log.error("[{}] MySQL 연결 실패", name, ex);
		}
		log.info("[{}] 서버 포트 {} — 예: http://localhost:{}/dashboard", name, port, port);
	}
}
