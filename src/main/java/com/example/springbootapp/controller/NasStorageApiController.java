package com.example.springbootapp.controller;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.NasFile;
import com.example.springbootapp.storage.NasMediaType;
import com.example.springbootapp.storage.NasStorageService;
import com.example.springbootapp.storage.NasStorageService.NasStoredFile;
import jakarta.servlet.http.HttpSession;
/**
 * NAS 스토리지 REST API.
 * <p>기본 경로: {@code /api/storage}</p>
 */
@RestController
@RequestMapping("/api/storage")
public class NasStorageApiController {
	private final NasStorageService nasStorageService;
	private final SessionAuthService sessionAuthService;
	public NasStorageApiController(NasStorageService nasStorageService, SessionAuthService sessionAuthService) {
		this.nasStorageService = nasStorageService;
		this.sessionAuthService = sessionAuthService;
	}
	/**
	 * 지원하는 미디어 유형 목록을 조회합니다.
	 *
	 * @return out: {@code ResponseEntity<Map>} — {@code types} (code, folder, label, maxMb, extensions)
	 */
	@GetMapping("/types")
	public ResponseEntity<Map<String, Object>> types() {
		List<Map<String, Object>> items = Arrays.stream(NasMediaType.values())
				.map(this::toTypeDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("types", items);
		return ResponseEntity.ok(body);
	}
	/**
	 * NAS에 저장된 파일 목록을 조회합니다.
	 *
	 * @param type  in: 미디어 유형 코드 (선택)
	 * @param limit in: 최대 조회 건수 (기본 50)
	 * @return out: {@code ResponseEntity<Map>} — {@code files}, {@code count}
	 */
	@GetMapping("/files")
	public ResponseEntity<Map<String, Object>> files(
			@RequestParam(required = false) String type,
			@RequestParam(defaultValue = "50") int limit) {
		List<Map<String, Object>> files = nasStorageService.search(type, limit).stream()
				.map(this::toFileDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("files", files);
		body.put("count", files.size());
		return ResponseEntity.ok(body);
	}
	/**
	 * 파일을 NAS 스토리지에 업로드합니다.
	 *
	 * @param file    in: 업로드할 멀티파트 파일
	 * @param type    in: 미디어 유형 코드 (기본 image)
	 * @param session in: 등록자 식별용 HTTP 세션
	 * @return out: {@code ResponseEntity<Map>} — {@code success}, {@code fileId}, {@code url} 등 또는 400
	 */
	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> upload(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "type", defaultValue = "image") String type,
			HttpSession session) {
		NasMediaType mediaType = NasMediaType.fromCode(type)
				.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 유형입니다: " + type));
		try {
			NasStoredFile stored = nasStorageService.store(mediaType, file, resolveRegId(session));
			Map<String, Object> body = toStoredDto(stored);
			body.put("success", true);
			body.put("message", stored.type().getLabel() + " 업로드 완료");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException | IOException ex) {
			return badRequest(ex.getMessage());
		}
	}
	private Map<String, Object> toStoredDto(NasStoredFile stored) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("fileId", stored.fileId());
		body.put("type", stored.type().name().toLowerCase());
		body.put("folder", stored.type().getFolderName());
		body.put("filename", stored.filename());
		body.put("url", stored.url());
		body.put("filePath", stored.filePath());
		body.put("originalName", stored.originalName());
		body.put("size", stored.sizeBytes());
		return body;
	}
	private Map<String, Object> toFileDto(NasFile row) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("fileId", row.getFileId());
		body.put("type", row.getMediaTypeCd() != null ? row.getMediaTypeCd().toLowerCase() : null);
		body.put("filename", row.getStoredNm());
		body.put("originalName", row.getOriginalNm());
		body.put("url", row.getUrlPath());
		body.put("filePath", row.getFilePath());
		body.put("size", row.getFileSize());
		body.put("regId", row.getRegId());
		body.put("regDt", row.getRegDt());
		return body;
	}
	private Map<String, Object> toTypeDto(NasMediaType type) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("code", type.name().toLowerCase());
		row.put("folder", type.getFolderName());
		row.put("label", type.getLabel());
		row.put("maxMb", type.getMaxBytes() / 1024 / 1024);
		row.put("extensions", type.getAllowedExtensions());
		return row;
	}
	private String resolveRegId(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		return userId != null && !userId.isBlank() ? userId : "SYSTEM";
	}
	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
}
