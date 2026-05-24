package com.example.springbootapp.util;
import jakarta.servlet.http.HttpServletRequest;
/**
 * 접속 로그용 클라이언트 IP·장비(User-Agent) 정보 추출
 */
public final class ClientDeviceResolver {
	private ClientDeviceResolver() {
	}
	public static String resolveClientIp(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String[] headers = {
				"CF-Connecting-IP",
				"X-Forwarded-For",
				"X-Real-IP",
				"Proxy-Client-IP",
				"WL-Proxy-Client-IP"
		};
		for (String header : headers) {
			String value = request.getHeader(header);
			if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
				int comma = value.indexOf(',');
				String ip = (comma > 0 ? value.substring(0, comma) : value).trim();
				return normalizeIp(trim(ip, 45));
			}
		}
		return normalizeIp(trim(request.getRemoteAddr(), 45));
	}
	public static ClientDeviceInfo resolveDevice(HttpServletRequest request) {
		if (request == null) {
			return ClientDeviceInfo.empty();
		}
		String userAgent = request.getHeader("User-Agent");
		return parseUserAgent(userAgent);
	}
	public static ClientDeviceInfo parseUserAgent(String userAgent) {
		if (userAgent == null || userAgent.isBlank()) {
			return ClientDeviceInfo.empty();
		}
		String ua = userAgent.trim();
		String lower = ua.toLowerCase();
		String deviceType = "DESKTOP";
		if (lower.contains("ipad") || (lower.contains("tablet") && !lower.contains("mobile"))) {
			deviceType = "TABLET";
		} else if (lower.contains("mobile") || lower.contains("iphone") || lower.contains("android")) {
			deviceType = "MOBILE";
		}
		String os = resolveOs(ua, lower);
		String browser = resolveBrowser(ua, lower);
		String model = resolveModel(ua, lower, deviceType);
		return new ClientDeviceInfo(
				trim(deviceType, 20),
				trim(os, 80),
				trim(browser, 80),
				trim(model, 120));
	}
	private static String resolveOs(String ua, String lower) {
		if (lower.contains("windows nt 10.0")) {
			return "Windows 10+";
		}
		if (lower.contains("windows nt 6.3")) {
			return "Windows 8.1";
		}
		if (lower.contains("windows nt 6.1")) {
			return "Windows 7";
		}
		if (lower.contains("windows")) {
			return "Windows";
		}
		if (lower.contains("mac os x")) {
			int idx = lower.indexOf("mac os x");
			String part = ua.substring(idx).replace('_', '.');
			if (part.length() > 24) {
				part = part.substring(0, 24);
			}
			return part;
		}
		if (lower.contains("android")) {
			int idx = lower.indexOf("android");
			String part = ua.substring(idx);
			int end = part.indexOf(';');
			if (end > 0) {
				part = part.substring(0, end);
			}
			return trim(part, 80);
		}
		if (lower.contains("iphone") || lower.contains("ipad") || lower.contains("ios")) {
			return "iOS";
		}
		if (lower.contains("linux")) {
			return "Linux";
		}
		return "Unknown";
	}
	private static String resolveBrowser(String ua, String lower) {
		if (lower.contains("edg/")) {
			return "Edge";
		}
		if (lower.contains("opr/") || lower.contains("opera")) {
			return "Opera";
		}
		if (lower.contains("chrome/") && !lower.contains("edg/")) {
			return "Chrome";
		}
		if (lower.contains("firefox/")) {
			return "Firefox";
		}
		if (lower.contains("safari/") && !lower.contains("chrome/")) {
			return "Safari";
		}
		if (lower.contains("msie") || lower.contains("trident/")) {
			return "IE";
		}
		return "Unknown";
	}
	private static String resolveModel(String ua, String lower, String deviceType) {
		if ("DESKTOP".equals(deviceType)) {
			return null;
		}
		if (lower.contains("iphone")) {
			return "iPhone";
		}
		if (lower.contains("ipad")) {
			return "iPad";
		}
		if (lower.contains("android")) {
			int semi = ua.indexOf(';');
			if (semi >= 0 && semi + 1 < ua.length()) {
				String segment = ua.substring(semi + 1).trim();
				int nextSemi = segment.indexOf(';');
				if (nextSemi > 0) {
					segment = segment.substring(0, nextSemi).trim();
				}
				if (!segment.isBlank() && !segment.toLowerCase().startsWith("build")) {
					return segment;
				}
			}
			return "Android";
		}
		return null;
	}
	private static String normalizeIp(String ip) {
		if (ip == null) {
			return null;
		}
		if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
			return "127.0.0.1";
		}
		if (ip.startsWith("::ffff:")) {
			return ip.substring(7);
		}
		return ip;
	}
	private static String trim(String value, int maxLen) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		if (t.isEmpty()) {
			return null;
		}
		return t.length() <= maxLen ? t : t.substring(0, maxLen);
	}
	public static final class ClientDeviceInfo {
		private final String deviceTypeCd;
		private final String deviceOs;
		private final String deviceBrowser;
		private final String deviceModel;
		public ClientDeviceInfo(String deviceTypeCd, String deviceOs, String deviceBrowser, String deviceModel) {
			this.deviceTypeCd = deviceTypeCd;
			this.deviceOs = deviceOs;
			this.deviceBrowser = deviceBrowser;
			this.deviceModel = deviceModel;
		}
		public static ClientDeviceInfo empty() {
			return new ClientDeviceInfo(null, null, null, null);
		}
		public String getDeviceTypeCd() {
			return deviceTypeCd;
		}
		public String getDeviceOs() {
			return deviceOs;
		}
		public String getDeviceBrowser() {
			return deviceBrowser;
		}
		public String getDeviceModel() {
			return deviceModel;
		}
	}
}
