package com.example.springbootapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.example.springbootapp.domain.ScreenList;

/**
 * fragments/sidebar.html 에서 th:href 메뉴 링크를 파싱해 screen_list 항목을 생성합니다.
 */
public final class ScreenSidebarLoader {

	private static final Logger log = LoggerFactory.getLogger(ScreenSidebarLoader.class);

	private static final Pattern ANCHOR = Pattern.compile(
			"<a\\s[^>]*th:href=\"@\\{([^}#][^}]*)\\}\"[^>]*>(.*?)</a>",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern LABEL = Pattern.compile(
			"nav-link-text\\s+ps-1\">\\s*([^<]+?)\\s*(?:</span>|<span)",
			Pattern.DOTALL);

	private ScreenSidebarLoader() {
	}

	private static volatile Map<String, String> uriLabelCache;

	public static String labelForUri(String uriPath) {
		String uri = normalizeUri(uriPath);
		if (uri == null) {
			return "";
		}
		Map<String, String> labels = uriLabelCache;
		if (labels == null) {
			synchronized (ScreenSidebarLoader.class) {
				labels = uriLabelCache;
				if (labels == null) {
					Map<String, String> built = new LinkedHashMap<>();
					for (ScreenList screen : fromSidebar()) {
						built.put(screen.getUriPath(), screen.getScreenNm());
					}
					uriLabelCache = Map.copyOf(built);
					labels = uriLabelCache;
				}
			}
		}
		return labels.getOrDefault(uri, fallbackLabel(uri));
	}

	public static String resolveDisplayName(String screenNm, String uriPath) {
		if (screenNm != null && !screenNm.isBlank()) {
			return screenNm.trim();
		}
		return labelForUri(uriPath);
	}

	public static List<ScreenList> fromSidebar() {
		String html;
		try {
			html = readSidebarHtml();
		} catch (IOException ex) {
			log.warn("sidebar.html 읽기 실패: {}", ex.getMessage());
			return List.of();
		}
		Map<String, ScreenList> byUri = new LinkedHashMap<>();
		Matcher matcher = ANCHOR.matcher(html);
		int sort = 10;
		while (matcher.find()) {
			String uri = normalizeUri(matcher.group(1).trim());
			if (uri == null || uri.isBlank()) {
				continue;
			}
			if (byUri.containsKey(uri)) {
				continue;
			}
			String label = extractLabel(matcher.group(2));
			if (label.isBlank()) {
				label = fallbackLabel(uri);
			}
			ScreenList screen = new ScreenList();
			screen.setScreenId(uriToScreenId(uri));
			screen.setScreenNm(label);
			screen.setUriPath(uri);
			screen.setTemplatePath(templateFromUri(uri));
			screen.setSortOrd(sort);
			screen.setUseYn("Y");
			byUri.put(uri, screen);
			sort += 10;
		}
		log.info("sidebar 메뉴 파싱 — {}건 (고유 URL)", byUri.size());
		return new ArrayList<>(byUri.values());
	}

	static String readSidebarHtml() throws IOException {
		Path devPath = Path.of("src/main/resources/templates/fragments/sidebar.html");
		if (Files.isRegularFile(devPath)) {
			return Files.readString(devPath, StandardCharsets.UTF_8);
		}
		ClassPathResource resource = new ClassPathResource("templates/fragments/sidebar.html");
		try (InputStream in = resource.getInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	static String extractLabel(String anchorBody) {
		Matcher labelMatcher = LABEL.matcher(anchorBody);
		if (labelMatcher.find()) {
			return decodeHtml(labelMatcher.group(1).trim());
		}
		return "";
	}

	static String normalizeUri(String uri) {
		if (uri == null || uri.isBlank()) {
			return null;
		}
		String path = uri.trim();
		if (path.endsWith(".html")) {
			path = path.substring(0, path.length() - 5);
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

	static String uriToScreenId(String uri) {
		if ("/".equals(uri)) {
			return "HOME";
		}
		String id = uri.substring(1).replace("/", "_").replace("-", "_").toUpperCase();
		if (id.length() > 100) {
			id = id.substring(0, 100);
		}
		return id;
	}

	static String templateFromUri(String uri) {
		if ("/".equals(uri)) {
			return "index";
		}
		return uri.startsWith("/") ? uri.substring(1) : uri;
	}

	static String fallbackLabel(String uri) {
		if ("/".equals(uri)) {
			return "대시보드 기본";
		}
		String segment = uri;
		int slash = segment.lastIndexOf('/');
		if (slash >= 0 && slash < segment.length() - 1) {
			segment = segment.substring(slash + 1);
		}
		return segment.replace("-", " ");
	}

	static String decodeHtml(String text) {
		return text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
	}
}
