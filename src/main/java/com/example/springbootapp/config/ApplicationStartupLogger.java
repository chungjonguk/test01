package com.example.springbootapp.config;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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
		log.info("[{}] PC 접속 — http://localhost:{}/dashboard", name, port);
		for (String lanIp : resolveLanIpv4Addresses()) {
			log.info("[{}] 모바일(LAN) 접속 — http://{}:{}/ (같은 Wi-Fi)", name, lanIp, port);
		}
	}
	private static List<String> resolveLanIpv4Addresses() {
		List<String> ips = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
					continue;
				}
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address inet4 && !inet4.isLoopbackAddress()) {
						String host = inet4.getHostAddress();
						if (host != null && !host.startsWith("169.254.")) {
							ips.add(host);
						}
					}
				}
			}
		} catch (Exception ex) {
			return Collections.emptyList();
		}
		return ips;
	}
}
