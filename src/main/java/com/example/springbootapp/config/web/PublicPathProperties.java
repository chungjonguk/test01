package com.example.springbootapp.config.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 공개 URL 암호화 설정 ({@code app.public-path.*}).
 */
@Component
@ConfigurationProperties(prefix = "app.public-path")
public class PublicPathProperties {

	/** 암호화 URL 사용 여부 */
	private boolean enabled = true;

	/** AES 키 원문 (SHA-256으로 32바이트 키 유도) */
	private String secret = "PrintMall-PublicPath-ChangeInProduction";

	/** 공개 경로 접두사 */
	private String prefix = "/e";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
