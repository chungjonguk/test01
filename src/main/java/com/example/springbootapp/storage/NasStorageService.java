package com.example.springbootapp.storage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
