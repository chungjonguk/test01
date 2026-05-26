package com.example.springbootapp.storage;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
public enum NasMediaType {
	IMAGE("images", "이미지", Set.of("jpg", "jpeg", "png", "gif", "webp", "svg", "bmp"), 10L * 1024 * 1024),
	DOCUMENT("documents", "문서", Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "hwp", "zip"), 20L * 1024 * 1024),
	VIDEO("videos", "영상", Set.of("mp4", "webm", "mov", "avi", "mkv"), 100L * 1024 * 1024),
	PRODUCT("products", "상품 이미지", Set.of("jpg", "jpeg", "png", "gif", "webp"), 5L * 1024 * 1024),
	COMPANY_PAGE("company-pages", "업체 페이지 이미지", Set.of("jpg", "jpeg", "png", "gif", "webp", "svg"), 10L * 1024 * 1024),
	SSL_CERT("ssl-certs", "SSL 인증서", Set.of("pem", "crt", "cer", "der"), 2L * 1024 * 1024);
	private final String folderName;
	private final String label;
	private final Set<String> allowedExtensions;
	private final long maxBytes;
	NasMediaType(String folderName, String label, Set<String> allowedExtensions, long maxBytes) {
		this.folderName = folderName;
		this.label = label;
		this.allowedExtensions = allowedExtensions;
		this.maxBytes = maxBytes;
	}
	public String getFolderName() {
		return folderName;
	}
	public String getLabel() {
		return label;
	}
	public Set<String> getAllowedExtensions() {
		return allowedExtensions;
	}
	public long getMaxBytes() {
		return maxBytes;
	}
	public static Optional<NasMediaType> fromCode(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		String normalized = code.trim().toLowerCase(Locale.ROOT);
		return Arrays.stream(values())
				.filter(t -> t.name().equalsIgnoreCase(normalized) || t.folderName.equalsIgnoreCase(normalized))
				.findFirst();
	}
}
