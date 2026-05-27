package com.example.springbootapp.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 요청 Host로 업체(테넌트)를 자동 선택하는 설정 ({@code app.tenant-host.*}).
 */
@Component
@ConfigurationProperties(prefix = "app.tenant-host")
public class TenantHostProperties {

	/** Host 기반 업체 자동 선택 */
	private boolean enabled = true;

	/** 테넌트 매핑을 적용하지 않을 호스트 (로컬·기본 접속) */
	private List<String> ignoreHosts = new ArrayList<>(List.of("localhost", "127.0.0.1", "0.0.0.0", "[::1]"));

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<String> getIgnoreHosts() {
		return ignoreHosts;
	}

	public void setIgnoreHosts(List<String> ignoreHosts) {
		this.ignoreHosts = ignoreHosts != null ? ignoreHosts : new ArrayList<>();
	}
}
