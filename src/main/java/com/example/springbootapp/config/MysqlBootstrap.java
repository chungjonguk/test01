package com.example.springbootapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Spring Boot 기동 전( main ) Windows에서 MySQL 기동·접속 확인 스크립트 실행.
 */
public final class MysqlBootstrap {

	private static final String PROP_AUTO_BOOTSTRAP = "app.datasource.auto-bootstrap-mysql";

	private MysqlBootstrap() {
	}

	public static void ensureReadyBeforeStartup(String[] args) {
		if (isTestProfile(args)) {
			return;
		}
		if (!isAutoBootstrapEnabled()) {
			System.out.println("[DB] MySQL 자동 기동 비활성화 (" + PROP_AUTO_BOOTSTRAP + "=false)");
			return;
		}
		if (!isWindows()) {
			System.out.println("[DB] MySQL 자동 기동은 Windows에서만 지원합니다. MySQL을 수동으로 기동하세요.");
			return;
		}

		Path script = Path.of(System.getProperty("user.dir"), "scripts", "ensure-mysql.bat");
		if (!Files.isRegularFile(script)) {
			System.err.println("[DB] 스크립트 없음: " + script.toAbsolutePath());
			System.err.println("       run-server.bat 또는 start-mysql.bat 로 MySQL을 먼저 기동하세요.");
			return;
		}

		System.out.println("[DB] MySQL 접속 확인 및 미기동 시 자동 시작 — " + script.toAbsolutePath());
		int exitCode = runEnsureMysqlScript(script);
		if (exitCode != 0) {
			System.err.println();
			System.err.println("[중단] DB 접속에 실패하여 Spring Boot를 시작하지 않습니다. (exit=" + exitCode + ")");
			System.err.println("  - start-mysql.bat 또는 scripts\\ensure-mysql.bat 실행 후 다시 시도");
			System.err.println("  - 127.0.0.1:3306, DB spring_boot_app, application.properties 계정 확인");
			System.exit(exitCode != 0 ? exitCode : 1);
		}
		System.out.println("[DB] 접속 확인 완료 — Spring Boot 기동을 계속합니다.");
		System.out.println();
	}

	private static int runEnsureMysqlScript(Path script) {
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", script.toAbsolutePath().toString());
		pb.directory(script.getParent().getParent().toFile());
		pb.inheritIO();
		try {
			Process process = pb.start();
			return process.waitFor();
		} catch (IOException | InterruptedException ex) {
			Thread.currentThread().interrupt();
			System.err.println("[DB] ensure-mysql.bat 실행 오류: " + ex.getMessage());
			return 1;
		}
	}

	private static boolean isTestProfile(String[] args) {
		if ("test".equalsIgnoreCase(System.getProperty("spring.profiles.active"))) {
			return true;
		}
		for (String arg : args) {
			if (arg != null && (arg.contains("spring.profiles.active=test") || arg.equals("--spring.profiles.active=test"))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isAutoBootstrapEnabled() {
		String sys = System.getProperty(PROP_AUTO_BOOTSTRAP);
		if (sys != null && !sys.isBlank()) {
			return Boolean.parseBoolean(sys.trim());
		}
		Properties props = loadApplicationProperties();
		String val = props.getProperty(PROP_AUTO_BOOTSTRAP, "true");
		return Boolean.parseBoolean(val.trim());
	}

	private static Properties loadApplicationProperties() {
		Properties props = new Properties();
		Path base = Path.of(System.getProperty("user.dir"));
		Path[] candidates = {
				base.resolve("src/main/resources/application.properties"),
				base.resolve("target/classes/application.properties")
		};
		for (Path path : candidates) {
			if (!Files.isRegularFile(path)) {
				continue;
			}
			try (InputStream in = Files.newInputStream(path)) {
				props.load(in);
				return props;
			} catch (IOException ignored) {
				// try next
			}
		}
		return props;
	}

	private static boolean isWindows() {
		String os = System.getProperty("os.name", "");
		return os.toLowerCase().contains("win");
	}
}
