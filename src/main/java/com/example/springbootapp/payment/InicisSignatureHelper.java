package com.example.springbootapp.payment;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
/**
 * KG이니시스 표준결제 SHA256 (NVP, 필드 알파벳순).
 * @see <a href="https://manual.inicis.com">이니시스 매뉴얼</a>
 */
public final class InicisSignatureHelper {
	private InicisSignatureHelper() {
	}
	public static String paymentSignature(String oid, String price, String timestamp) {
		return sha256(nvp(Map.of("oid", oid, "price", price, "timestamp", timestamp)));
	}
	public static String paymentVerification(String oid, String price, String signKey, String timestamp) {
		return sha256(nvp(Map.of("oid", oid, "price", price, "signKey", signKey, "timestamp", timestamp)));
	}
	public static String mKey(String signKey) {
		return sha256(signKey);
	}
	public static String approveSignature(String authToken, String timestamp) {
		return sha256(nvp(Map.of("authToken", authToken, "timestamp", timestamp)));
	}
	public static String approveVerification(String authToken, String signKey, String timestamp) {
		return sha256(nvp(Map.of("authToken", authToken, "signKey", signKey, "timestamp", timestamp)));
	}
	private static String nvp(Map<String, String> fields) {
		return new TreeMap<>(fields).entrySet().stream()
				.map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining("&"));
	}
	public static String sha256(String plain) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}
}
