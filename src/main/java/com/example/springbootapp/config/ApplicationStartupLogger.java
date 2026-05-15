package com.example.springbootapp.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import com.example.springbootapp.repository.ShopOrderRepository;

@Configuration
public class ApplicationStartupLogger implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(ApplicationStartupLogger.class);

	private final DataSource dataSource;
	private final ShopOrderRepository shopOrderRepository;

	public ApplicationStartupLogger(DataSource dataSource, ShopOrderRepository shopOrderRepository) {
		this.dataSource = dataSource;
		this.shopOrderRepository = shopOrderRepository;
	}

	@Override
	public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
		if (!(event.getApplicationContext() instanceof WebServerApplicationContext webCtx)) {
			return;
		}
		int port = webCtx.getWebServer().getPort();
		String name = event.getApplicationContext().getEnvironment().getProperty("spring.application.name", "app");
		try (var connection = dataSource.getConnection()) {
			log.info("[{}] MySQL 연결 성공 — url={}, 주문 건수={}",
					name, connection.getMetaData().getURL(), shopOrderRepository.count());
		} catch (Exception ex) {
			log.error("[{}] MySQL 연결 실패", name, ex);
		}
		log.info("[{}] 서버 포트 {} — 주문 상세 예: http://localhost:{}/app/e-commerce/orders/order-details.html",
				name, port, port);
	}
}
