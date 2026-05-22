package com.example.springbootapp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorageService {

	private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
	private static final long MAX_BYTES = 5 * 1024 * 1024;

	private final Path uploadDir;

	public ProductImageStorageService() {
		this.uploadDir = Paths.get("src", "main", "resources", "static", "uploads", "products").toAbsolutePath();
	}

	public String store(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어 있습니다.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new IllegalArgumentException("이미지는 5MB 이하만 업로드할 수 있습니다.");
		}
		String ext = resolveExtension(file.getOriginalFilename(), file.getContentType());
		if (!ALLOWED_EXT.contains(ext)) {
			throw new IllegalArgumentException("jpg, png, gif, webp 형식만 업로드할 수 있습니다.");
		}
		Files.createDirectories(uploadDir);
		String filename = UUID.randomUUID() + "." + ext;
		Path target = uploadDir.resolve(filename);
		file.transferTo(target);
		return "/uploads/products/" + filename;
	}

	private static String resolveExtension(String originalName, String contentType) {
		if (originalName != null && originalName.contains(".")) {
			String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
			if (ALLOWED_EXT.contains(ext)) {
				return ext;
			}
		}
		if (contentType != null) {
			return switch (contentType.toLowerCase(Locale.ROOT)) {
				case "image/jpeg" -> "jpg";
				case "image/png" -> "png";
				case "image/gif" -> "gif";
				case "image/webp" -> "webp";
				default -> "jpg";
			};
		}
		return "jpg";
	}
}
