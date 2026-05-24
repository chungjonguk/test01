package com.example.springbootapp.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component
@ConfigurationProperties(prefix = "payment.inicis")
public class InicisProperties {
	/** false면 결제창 대신 모의 승인 */
	private boolean enabled = true;
	/** true면 이니시스 없이 로컬 모의 결제 */
	private boolean mockEnabled = true;
	private String mid = "INIpayTest";
	private String signKey = "SU5JTElURV9UUklQTEVERVNfS0VZU1RS";
	private String stdPayJsUrl = "https://stgstdpay.inicis.com/stdjs/INIStdPay.js";
	private String baseUrl = "http://localhost:8081";
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isMockEnabled() {
		return mockEnabled;
	}
	public void setMockEnabled(boolean mockEnabled) {
		this.mockEnabled = mockEnabled;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getSignKey() {
		return signKey;
	}
	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}
	public String getStdPayJsUrl() {
		return stdPayJsUrl;
	}
	public void setStdPayJsUrl(String stdPayJsUrl) {
		this.stdPayJsUrl = stdPayJsUrl;
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String returnUrl() {
		return trimSlash(baseUrl) + "/app/e-commerce/checkout/inicis/return";
	}
	public String closeUrl() {
		return trimSlash(baseUrl) + "/app/e-commerce/checkout/inicis/close";
	}
	public boolean useRealGateway() {
		return enabled && !mockEnabled && signKey != null && !signKey.isBlank()
				&& !signKey.contains("여기에");
	}
	private static String trimSlash(String url) {
		if (url == null) {
			return "";
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
