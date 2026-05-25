package com.example.springbootapp.config.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * 컨트롤러·API 경로를 {@code /e/{암호문}.do} 공개 URL로 변환·복호화합니다.
 * <p>경로별 고정 IV(해시) + AES-CTR로 동일 경로는 항상 동일 토큰이 됩니다(재기동·링크 안정).</p>
 */
@Service
@Profile("!test")
public class PublicPathCryptoService {

	private static final String TRANSFORM = "AES/CTR/NoPadding";

	private final PublicPathProperties properties;
	private final byte[] aesKey;

	public PublicPathCryptoService(PublicPathProperties properties) {
		this.properties = properties;
		this.aesKey = deriveKey(properties.getSecret());
	}

	public boolean isEnabled() {
		return properties.isEnabled();
	}

	/** 논리 경로 → 공개 URL ({@code /e/토큰.do}) */
	public String toPublicPath(String path) {
		if (!isEnabled() || DoPathHelper.shouldSkipSuffix(path)) {
			return path == null || path.isBlank() ? "/" : stripQuery(path.trim());
		}
		String canonical = DoPathHelper.toDoPath(path);
		if (isAlreadyPublic(canonical)) {
			return canonical;
		}
		String token = encryptCanonical(canonical);
		return properties.getPrefix() + "/" + token + ".do";
	}

	/** 요청 경로 → 논리 경로 ({@code *.do}) */
	public String toLogicalPath(String requestPath) {
		if (requestPath == null || requestPath.isBlank()) {
			return "/";
		}
		String path = stripQuery(requestPath.trim());
		if (!isEnabled() || !isPublicPath(path)) {
			return DoPathHelper.stripDoSuffix(path);
		}
		String token = extractToken(path);
		if (token == null) {
			return DoPathHelper.stripDoSuffix(path);
		}
		try {
			return decryptToken(token);
		} catch (Exception ex) {
			return path;
		}
	}

	public boolean isPublicPath(String path) {
		if (path == null || !isEnabled()) {
			return false;
		}
		String p = stripQuery(path);
		String prefix = properties.getPrefix() + "/";
		return p.startsWith(prefix) && p.endsWith(".do") && p.length() > prefix.length() + 4;
	}

	public RequestMappingInfo addEncryptedMapping(RequestMappingInfo info) {
		if (!isEnabled()) {
			return DoPathHelper.addDoSuffix(info);
		}
		PathPatternsRequestCondition paths = info.getPathPatternsCondition();
		if (paths == null || paths.getPatterns().isEmpty()) {
			return info;
		}
		Set<String> mapped = new LinkedHashSet<>();
		for (var pattern : paths.getPatterns()) {
			String raw = pattern.getPatternString();
			if (DoPathHelper.shouldSkipSuffix(raw) || raw.contains("{") || raw.contains("*")) {
				mapped.add(raw);
			} else {
				mapped.add(toPublicPath(raw));
			}
		}
		if (mapped.isEmpty()) {
			return info;
		}
		return info.mutate().paths(mapped.toArray(String[]::new)).build();
	}

	private boolean isAlreadyPublic(String path) {
		return isPublicPath(path);
	}

	private String encryptCanonical(String canonical) {
		try {
			byte[] plain = canonical.getBytes(StandardCharsets.UTF_8);
			Cipher cipher = Cipher.getInstance(TRANSFORM);
			byte[] iv = deriveIv(canonical);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
			byte[] encrypted = cipher.doFinal(plain);
			byte[] combined = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
		} catch (Exception ex) {
			throw new IllegalStateException("경로 암호화 실패: " + canonical, ex);
		}
	}

	private String decryptToken(String token) throws Exception {
		byte[] combined = Base64.getUrlDecoder().decode(token);
		if (combined.length < 17) {
			throw new IllegalArgumentException("invalid token");
		}
		byte[] iv = Arrays.copyOfRange(combined, 0, 16);
		byte[] body = Arrays.copyOfRange(combined, 16, combined.length);
		Cipher cipher = Cipher.getInstance(TRANSFORM);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
		String plain = new String(cipher.doFinal(body), StandardCharsets.UTF_8);
		if (!DoPathHelper.toDoPath(plain).equals(plain)) {
			throw new IllegalArgumentException("invalid logical path");
		}
		return plain;
	}

	private String extractToken(String publicPath) {
		String prefix = properties.getPrefix() + "/";
		if (!publicPath.startsWith(prefix) || !publicPath.endsWith(".do")) {
			return null;
		}
		return publicPath.substring(prefix.length(), publicPath.length() - 3);
	}

	private byte[] deriveIv(String canonical) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(aesKey);
			digest.update(canonical.getBytes(StandardCharsets.UTF_8));
			return Arrays.copyOf(digest.digest(), 16);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static byte[] deriveKey(String secret) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static String stripQuery(String uri) {
		int q = uri.indexOf('?');
		return q >= 0 ? uri.substring(0, q) : uri;
	}
}
