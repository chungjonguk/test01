package com.example.springbootapp.config;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 서버 기동 직후 MySQL 접속 가능 여부를 확인합니다. 실패 시 기동을 중단합니다.
 */
@Profile("!test")
@Component
@Order(1)
public class DatabaseStartupValidator implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseStartupValidator.class);

	private final DataSource dataSource;

	@Value("${app.datasource.startup-check:true}")
	private boolean startupCheckEnabled;

	@Value("${app.datasource.startup-check-max-wait-sec:30}")
	private int maxWaitSec;

	@Value("${app.datasource.startup-check-retry-sec:2}")
	private int retrySec;

	public DatabaseStartupValidator(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!startupCheckEnabled) {
			log.warn("DB 기동 검사 비활성화 (app.datasource.startup-check=false)");
			return;
		}

		log.info("MySQL 접속 재확인 중... (최대 {}초, main 기동 시 ensure-mysql 이미 실행됨)", maxWaitSec);
		long deadline = System.currentTimeMillis() + maxWaitSec * 1000L;
		Exception lastError = null;

		while (System.currentTimeMillis() < deadline) {
			try (Connection connection = dataSource.getConnection()) {
				if (!connection.isValid(5)) {
					throw new IllegalStateException("Connection.isValid(5) == false");
				}
				var meta = connection.getMetaData();
				log.info("MySQL 접속 OK — url={}, user={}, product={} {}",
						meta.getURL(),
						meta.getUserName(),
						meta.getDatabaseProductName(),
						meta.getDatabaseProductVersion());
				return;
			} catch (Exception ex) {
				lastError = ex;
				long remaining = deadline - System.currentTimeMillis();
				if (remaining <= 0) {
					break;
				}
				log.warn("MySQL 접속 대기 중... ({}초 후 재시도) — {}",
						retrySec,
						rootMessage(ex));
				Thread.sleep(retrySec * 1000L);
			}
		}

		String hint = """
				MySQL 접속에 실패하여 서버를 시작할 수 없습니다.
				  1) start-mysql.bat 또는 scripts\\ensure-mysql.bat 실행
				  2) 127.0.0.1:3306 / DB spring_boot_app / 계정 application.properties 확인
				  3) run-server.bat 사용 시 [2/4] DB 확인 단계 통과 여부 확인
				""";
		log.error(hint);
		IllegalStateException failure = new IllegalStateException(
				"MySQL 접속 실패 (Connection refused 등). MySQL 기동 후 다시 실행하세요.",
				lastError);
		throw failure;
	}

	private static String rootMessage(Throwable t) {
		Throwable c = t;
		while (c.getCause() != null) {
			c = c.getCause();
		}
		return c.getMessage() != null ? c.getMessage() : c.getClass().getSimpleName();
	}
}
