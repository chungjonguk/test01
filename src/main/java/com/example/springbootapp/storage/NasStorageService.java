package com.example.springbootapp.storage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import com.example.springbootapp.dto.NasStorageUsageDto;
import com.example.springbootapp.dto.NasStorageUsageDto.CategoryUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.springbootapp.config.NasStorageProperties;
import com.example.springbootapp.domain.NasFile;
import com.example.springbootapp.mapper.NasFileMapper;
/**
 * NAS(네트워크 저장소)에 미디어 파일 업로드·메타데이터 저장·검색을 처리하는 서비스.
 */
@Service
public class NasStorageService {
	private static final Logger log = LoggerFactory.getLogger(NasStorageService.class);
	private final NasStorageProperties nasStorage;
	private final NasFileMapper nasFileMapper;
	public NasStorageService(NasStorageProperties nasStorage, NasFileMapper nasFileMapper) throws IOException {
		this.nasStorage = nasStorage;
		this.nasFileMapper = nasFileMapper;
		nasStorage.ensureAllMediaDirsExist();
		log.info("NAS 저장소: {} (enabled={})", nasStorage.resolveUploadRoot(), nasStorage.isEnabled());
	}
	/**
	 * 미디어 파일을 NAS에 저장하고 DB에 메타데이터를 등록한다.
	 *
	 * @param type  미디어 유형 (허용 확장자·용량 제한 적용)
	 * @param file  업로드 파일
	 * @param regId 등록자 ID
	 * @return 저장된 파일 정보 (ID, URL, 경로 등)
	 * @throws IOException 디렉터리 생성·파일 저장 실패 시
	 */
	@Transactional
	public NasStoredFile store(NasMediaType type, MultipartFile file, String regId) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어 있습니다.");
		}
		if (file.getSize() > type.getMaxBytes()) {
			throw new IllegalArgumentException(type.getLabel() + "는 "
					+ (type.getMaxBytes() / 1024 / 1024) + "MB 이하만 업로드할 수 있습니다.");
		}
		String ext = resolveExtension(file.getOriginalFilename(), file.getContentType(), type);
		if (!type.getAllowedExtensions().contains(ext)) {
			throw new IllegalArgumentException(type.getLabel() + " 형식이 아닙니다. 허용: "
					+ String.join(", ", type.getAllowedExtensions()));
		}
		Path uploadDir = nasStorage.resolveMediaDir(type);
		Files.createDirectories(uploadDir);
		String filename = UUID.randomUUID() + "." + ext;
		Path target = uploadDir.resolve(filename);
		file.transferTo(target);
		String urlPath = nasStorage.buildPublicUrl(type, filename);
		String absolutePath = target.toAbsolutePath().normalize().toString();
		NasFile row = new NasFile();
		row.setMediaTypeCd(type.name());
		row.setStoredNm(filename);
		row.setOriginalNm(trimToNull(file.getOriginalFilename()));
		row.setFileExt(ext);
		row.setFileSize(file.getSize());
		row.setFilePath(absolutePath);
		row.setUrlPath(urlPath);
		row.setContentType(trimToNull(file.getContentType()));
		row.setRegId(regId != null && !regId.isBlank() ? regId.trim() : "SYSTEM");
		nasFileMapper.insert(row);
		log.debug("NAS 파일 저장: id={}, path={}", row.getFileId(), absolutePath);
		return new NasStoredFile(
				row.getFileId(),
				type,
				filename,
				urlPath,
				absolutePath,
				file.getOriginalFilename(),
				file.getSize());
	}
	/**
	 * NAS에 저장된 파일 메타데이터를 검색한다.
	 *
	 * @param mediaTypeCd 미디어 유형 코드 (null이면 전체)
	 * @param limit       최대 조회 건수 (1~500)
	 * @return NAS 파일 메타데이터 목록
	 */
	/**
	 * NAS 업로드 루트 폴더별 사용량을 집계한다 (Using Storage 위젯용).
	 *
	 * @return 폴더별·합계 사용량
	 */
	public NasStorageUsageDto getUsageSummary() {
		long quotaBytes = nasStorage.getQuotaBytes();
		Path uploadRoot = nasStorage.resolveUploadRoot();
		long imageBytes = folderSizeBytes(nasStorage.resolveMediaDir(NasMediaType.IMAGE));
		long productBytes = folderSizeBytes(nasStorage.resolveMediaDir(NasMediaType.PRODUCT));
		long documentBytes = folderSizeBytes(nasStorage.resolveMediaDir(NasMediaType.DOCUMENT));
		long videoBytes = folderSizeBytes(nasStorage.resolveMediaDir(NasMediaType.VIDEO));
		List<CategoryUsage> categories = new ArrayList<>();
		categories.add(category("regular", "이미지·상품", "images+products", imageBytes + productBytes,
				"bg-progress-gradient border-end border-white border-2", "bg-primary", quotaBytes));
		categories.add(category("system", "문서", NasMediaType.DOCUMENT.getFolderName(), documentBytes,
				"bg-info border-end border-white border-2", "bg-info", quotaBytes));
		categories.add(category("shared", "영상", NasMediaType.VIDEO.getFolderName(), videoBytes,
				"bg-success border-end border-white border-2", "bg-success", quotaBytes));
		long usedBytes = imageBytes + productBytes + documentBytes + videoBytes;
		long freeBytes = Math.max(0, quotaBytes - usedBytes);
		applyBarPercents(categories, usedBytes, quotaBytes);
		CategoryUsage free = category("free", "여유", "-", freeBytes,
				"bg-200", "bg-200", quotaBytes);
		double usedSum = categories.stream().mapToDouble(CategoryUsage::getPercentOfQuota).sum();
		free.setPercentOfQuota(Math.round(Math.max(0, 100.0 - usedSum) * 100.0) / 100.0);
		categories.add(free);
		NasStorageUsageDto dto = new NasStorageUsageDto();
		dto.setUploadRoot(uploadRoot.toString());
		dto.setQuotaBytes(quotaBytes);
		dto.setUsedBytes(usedBytes);
		dto.setFreeBytes(freeBytes);
		dto.setCategories(categories);
		return dto;
	}
	private static void applyBarPercents(List<CategoryUsage> categories, long usedBytes, long quotaBytes) {
		if (usedBytes <= 0 || quotaBytes <= 0) {
			return;
		}
		double usedBarTotal = Math.min(100.0, usedBytes * 100.0 / quotaBytes);
		for (CategoryUsage row : categories) {
			double pct = row.getBytes() * usedBarTotal / usedBytes;
			row.setPercentOfQuota(Math.round(pct * 100.0) / 100.0);
		}
	}
	private static CategoryUsage category(
			String code, String label, String folder, long bytes, String barClass, String dotClass, long quotaBytes) {
		CategoryUsage row = new CategoryUsage();
		row.setCode(code);
		row.setLabel(label);
		row.setFolder(folder);
		row.setBytes(bytes);
		row.setBarClass(barClass);
		row.setDotClass(dotClass);
		row.setPercentOfQuota(0);
		return row;
	}
	private static long folderSizeBytes(Path dir) {
		if (dir == null || !Files.isDirectory(dir)) {
			return 0;
		}
		final long[] total = { 0 };
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					total[0] += attrs.size();
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			log.warn("NAS 폴더 용량 집계 실패: {}", dir, ex);
			return 0;
		}
		return total[0];
	}
	/**
	 * NAS 설정 및 폴더별 용량(디스크) 정보.
	 */
	public java.util.Map<String, Object> getConfigSummary() {
		java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
		body.put("enabled", nasStorage.isEnabled());
		body.put("basePath", nasStorage.getBasePath());
		body.put("uploadSubdir", nasStorage.getUploadSubdir());
		body.put("urlPrefix", nasStorage.normalizedUrlPrefix());
		body.put("quotaGb", nasStorage.getQuotaGb());
		body.put("quotaBytes", nasStorage.getQuotaBytes());
		body.put("uploadRoot", nasStorage.resolveUploadRoot().toString());
		List<java.util.Map<String, Object>> folders = new ArrayList<>();
		for (NasMediaType type : NasMediaType.values()) {
			long bytes = folderSizeBytes(nasStorage.resolveMediaDir(type));
			java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
			row.put("code", type.name().toLowerCase());
			row.put("label", type.getLabel());
			row.put("folder", type.getFolderName());
			row.put("path", nasStorage.resolveMediaDir(type).toString());
			row.put("bytes", bytes);
			row.put("mb", Math.round(bytes / 1024.0 / 1024.0 * 100.0) / 100.0);
			row.put("maxMb", type.getMaxBytes() / 1024 / 1024);
			row.put("extensions", type.getAllowedExtensions());
			folders.add(row);
		}
		body.put("folders", folders);
		return body;
	}
	@Transactional(readOnly = true)
	public List<NasFile> search(String mediaTypeCd, int limit) {
		String typeFilter = null;
		if (mediaTypeCd != null && !mediaTypeCd.isBlank()) {
			typeFilter = NasMediaType.fromCode(mediaTypeCd)
					.map(Enum::name)
					.orElse(mediaTypeCd.trim().toUpperCase(Locale.ROOT));
		}
		int safeLimit = Math.min(Math.max(limit, 1), 500);
		return nasFileMapper.search(typeFilter, safeLimit);
	}
	private static String resolveExtension(String originalName, String contentType, NasMediaType type) {
		if (originalName != null && originalName.contains(".")) {
			String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
			if (type.getAllowedExtensions().contains(ext)) {
				return ext;
			}
		}
		if (contentType != null) {
			String ct = contentType.toLowerCase(Locale.ROOT);
			if (ct.contains("jpeg")) {
				return "jpg";
			}
			if (ct.contains("png")) {
				return "png";
			}
			if (ct.contains("gif")) {
				return "gif";
			}
			if (ct.contains("webp")) {
				return "webp";
			}
			if (ct.contains("pdf")) {
				return "pdf";
			}
			if (ct.contains("mp4")) {
				return "mp4";
			}
			if (ct.contains("webm")) {
				return "webm";
			}
		}
		return type.getAllowedExtensions().iterator().next();
	}
	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
	/** NAS 저장 결과 (파일 ID, URL, 경로 등). */
	public record NasStoredFile(
			Long fileId,
			NasMediaType type,
			String filename,
			String url,
			String filePath,
			String originalName,
			long sizeBytes) {
	}
}
