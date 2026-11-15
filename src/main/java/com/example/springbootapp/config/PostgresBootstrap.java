package com.example.springbootapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring Boot 기동 전 PostgreSQL 접속 확인 및 대상 DB 생성.
 * <p>MySQL 전용 {@code MysqlBootstrap} 대체. JDBC URL·계정은 환경변수 또는 application*.properties 에서 읽습니다.</p>
 */
public final class PostgresBootstrap {

	private static final String PROP_AUTO_BOOTSTRAP = "app.datasource.auto-bootstrap-postgres";
	private static final Pattern JDBC_URL = Pattern.compile(
			"^jdbc:postgresql://([^/:]+)(?::(\\d+))?/([^?]+)(?:\\?.*)?$",
			Pattern.CASE_INSENSITIVE);

	private PostgresBootstrap() {
	}

	public static void ensureReadyBeforeStartup(String[] args) {
		if (isTestProfile(args)) {
			return;
		}
		if (!isAutoBootstrapEnabled()) {
			System.out.println("[DB] PostgreSQL 자동 준비 비활성화 (" + PROP_AUTO_BOOTSTRAP + "=false)");
			return;
		}

		Properties props = loadMergedProperties();
		String jdbcUrl = firstNonBlank(
				System.getenv("SPRING_DATASOURCE_URL"),
				System.getProperty("spring.datasource.url"),
				props.getProperty("spring.datasource.url"),
				"jdbc:postgresql://localhost:5432/spring_boot_app");
		String username = firstNonBlank(
				System.getenv("SPRING_DATASOURCE_USERNAME"),
				System.getProperty("spring.datasource.username"),
				props.getProperty("spring.datasource.username"),
				"postgres");
		String password = firstNonBlank(
				System.getenv("SPRING_DATASOURCE_PASSWORD"),
				System.getProperty("spring.datasource.password"),
				props.getProperty("spring.datasource.password"),
				"postgres");

		Matcher matcher = JDBC_URL.matcher(jdbcUrl.trim());
		if (!matcher.matches()) {
			System.err.println("[DB] PostgreSQL JDBC URL 형식이 아닙니다: " + jdbcUrl);
			System.err.println("       예) jdbc:postgresql://localhost:5432/spring_boot_app");
			System.exit(1);
		}

		String host = matcher.group(1);
		String port = matcher.group(2) != null ? matcher.group(2) : "5432";
		String database = matcher.group(3);
		String adminUrl = "jdbc:postgresql://" + host + ":" + port + "/postgres";

		System.out.println("[DB] PostgreSQL 접속 확인 — host=" + host + ":" + port
				+ ", database=" + database + ", user=" + username);

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException ex) {
			System.err.println("[DB] PostgreSQL JDBC 드라이버를 찾을 수 없습니다.");
			System.exit(1);
		}

		Exception last = null;
		for (int attempt = 1; attempt <= 15; attempt++) {
			try (Connection connection = DriverManager.getConnection(adminUrl, username, password);
					Statement statement = connection.createStatement()) {
				boolean exists = false;
				try (ResultSet rs = statement.executeQuery(
						"SELECT 1 FROM pg_database WHERE datname = '" + escapeLiteral(database) + "'")) {
					exists = rs.next();
				}
				if (!exists) {
					System.out.println("[DB] 데이터베이스 생성: " + database);
					statement.executeUpdate("CREATE DATABASE \"" + database.replace("\"", "") + "\" ENCODING 'UTF8'");
				} else {
					System.out.println("[DB] 데이터베이스 존재 확인: " + database);
				}
				try (Connection appConn = DriverManager.getConnection(jdbcUrl, username, password)) {
					if (!appConn.isValid(5)) {
						throw new IllegalStateException("대상 DB Connection.isValid == false");
					}
				}
				System.out.println("[DB] 접속 확인 완료 — Spring Boot 기동을 계속합니다.");
				System.out.println();
				return;
			} catch (Exception ex) {
				last = ex;
				System.out.println("[DB] PostgreSQL 대기 중... (" + attempt + "/15) — " + rootMessage(ex));
				try {
					Thread.sleep(2000L);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}

		System.err.println();
		System.err.println("[중단] PostgreSQL 접속에 실패하여 Spring Boot를 시작하지 않습니다.");
		System.err.println("  - PostgreSQL 기동: localhost:" + port);
		System.err.println("  - DB/계정: " + database + " / " + username);
		System.err.println("  - URL: " + jdbcUrl);
		if (last != null) {
			System.err.println("  - 원인: " + rootMessage(last));
		}
		System.exit(1);
	}

	private static boolean isAutoBootstrapEnabled() {
		String sys = System.getProperty(PROP_AUTO_BOOTSTRAP);
		if (sys != null && !sys.isBlank()) {
			return Boolean.parseBoolean(sys.trim());
		}
		String env = System.getenv("APP_DATASOURCE_AUTO_BOOTSTRAP_POSTGRES");
		if (env != null && !env.isBlank()) {
			return Boolean.parseBoolean(env.trim());
		}
		Properties props = loadMergedProperties();
		String val = props.getProperty(PROP_AUTO_BOOTSTRAP, "true");
		return Boolean.parseBoolean(val.trim());
	}

	private static Properties loadMergedProperties() {
		Properties props = new Properties();
		Path base = Path.of(System.getProperty("user.dir"));
		Path[] candidates = {
				base.resolve("src/main/resources/application.properties"),
				base.resolve("target/classes/application.properties"),
				base.resolve("src/main/resources/application-local.properties"),
				base.resolve("application-local.properties"),
				base.resolve("target/classes/application-local.properties")
		};
		for (Path path : candidates) {
			if (!Files.isRegularFile(path)) {
				continue;
			}
			try (InputStream in = Files.newInputStream(path)) {
				Properties loaded = new Properties();
				loaded.load(in);
				props.putAll(loaded);
			} catch (IOException ignored) {
				// try next
			}
		}
		return props;
	}

	private static boolean isTestProfile(String[] args) {
		if ("test".equalsIgnoreCase(System.getProperty("spring.profiles.active"))) {
			return true;
		}
		String env = System.getenv("SPRING_PROFILES_ACTIVE");
		if (env != null && env.toLowerCase().contains("test")) {
			return true;
		}
		for (String arg : args) {
			if (arg != null && (arg.contains("spring.profiles.active=test") || arg.equals("--spring.profiles.active=test"))) {
				return true;
			}
		}
		return false;
	}

	private static String firstNonBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (value != null && !value.isBlank() && !value.contains("${")) {
				return value.trim();
			}
		}
		return null;
	}

	private static String escapeLiteral(String value) {
		return value == null ? "" : value.replace("'", "''");
	}

	private static String rootMessage(Throwable t) {
		Throwable c = t;
		while (c.getCause() != null) {
			c = c.getCause();
		}
		return c.getMessage() != null ? c.getMessage() : c.getClass().getSimpleName();
	}
}
