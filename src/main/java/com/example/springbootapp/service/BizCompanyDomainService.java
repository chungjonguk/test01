package com.example.springbootapp.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import org.springframework.beans.factory.ObjectProvider;
import com.example.springbootapp.domain.BizCompany;
import com.example.springbootapp.domain.BizCompanyDomain;
import com.example.springbootapp.dto.BizCompanyDomainFormDto;
import com.example.springbootapp.mapper.BizCompanyDomainMapper;
import com.example.springbootapp.mapper.BizCompanyMapper;
import com.example.springbootapp.storage.NasMediaType;
import com.example.springbootapp.storage.NasStorageService;
import com.example.springbootapp.util.AppDateTimeFormats;
import com.example.springbootapp.util.SslCertificateParser;
import com.example.springbootapp.util.SslCertificateParser.ParsedSslCertificate;
import jakarta.servlet.http.HttpSession;

@Service
@Transactional(readOnly = true)
public class BizCompanyDomainService {

	private static final int MAX_LIMIT = 500;
	private static final Pattern HOST_PATTERN = Pattern.compile(
			"^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)(?:\\.(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?))*$",
			Pattern.CASE_INSENSITIVE);

	private static final int SSL_EXPIRING_SOON_DAYS = 30;

	private final BizCompanyDomainMapper bizCompanyDomainMapper;
	private final BizCompanyMapper bizCompanyMapper;
	private final SessionAuthService sessionAuthService;
	private final NasStorageService nasStorageService;
	private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;

	public BizCompanyDomainService(
			BizCompanyDomainMapper bizCompanyDomainMapper,
			BizCompanyMapper bizCompanyMapper,
			SessionAuthService sessionAuthService,
			NasStorageService nasStorageService,
			ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
		this.bizCompanyDomainMapper = bizCompanyDomainMapper;
		this.bizCompanyMapper = bizCompanyMapper;
		this.sessionAuthService = sessionAuthService;
		this.nasStorageService = nasStorageService;
		this.publicPathCrypto = publicPathCrypto;
	}

	public List<Map<String, Object>> search(Long companyId, String hostName, String useYn, int limit) {
		if (companyId == null) {
			throw new IllegalArgumentException("업체를 선택하세요.");
		}
		if (bizCompanyMapper.findById(companyId) == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다.");
		}
		int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
		return bizCompanyDomainMapper
				.search(companyId, trimToNull(hostName), trimToNull(useYn), safeLimit)
				.stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	public Map<String, Object> findById(Long domainId) {
		BizCompanyDomain row = bizCompanyDomainMapper.findById(domainId);
		return row == null ? null : toDto(row);
	}

	@Transactional
	public Map<String, Object> uploadSslCertificate(MultipartFile file, String hostName, HttpSession session)
			throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("SSL 인증서 파일을 선택하세요.");
		}
		ParsedSslCertificate parsed;
		try (var input = file.getInputStream()) {
			parsed = SslCertificateParser.parse(input, hostName);
		}
		String actor = resolveActor(session);
		var stored = nasStorageService.store(NasMediaType.SSL_CERT, file, actor);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", true);
		body.put("fileId", stored.fileId());
		body.put("sslCertNotBefore", AppDateTimeFormats.formatDateTime(parsed.notBefore()));
		body.put("sslCertNotAfter", AppDateTimeFormats.formatDateTime(parsed.notAfter()));
		body.put("sslCertSubject", parsed.subjectDn());
		body.put("sslCertIssuer", parsed.issuerDn());
		body.put("certificateCount", parsed.certificateCount());
		appendSslValidityFlags(body, parsed.notAfter());
		body.put("message", "SSL 인증서가 등록되었습니다. 유효기간을 확인한 뒤 저장하세요.");
		return body;
	}

	/**
	 * 신규 업체 등록 시 대표 접속 도메인을 함께 등록한다 (선택).
	 */
	@Transactional
	public void registerPrimaryHostForNewCompany(Long companyId, String rawHost, String actor) {
		if (companyId == null) {
			throw new IllegalArgumentException("업체 ID가 필요합니다.");
		}
		if (bizCompanyMapper.findById(companyId) == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다.");
		}
		String host = normalizeHostName(rawHost);
		if (host == null || host.isBlank()) {
			return;
		}
		if (host.length() > 253) {
			throw new IllegalArgumentException("도메인은 253자 이하여야 합니다.");
		}
		if (!HOST_PATTERN.matcher(host).matches()) {
			throw new IllegalArgumentException("올바른 도메인 형식이 아닙니다. (예: shop.example.com)");
		}
		BizCompanyDomain duplicate = bizCompanyDomainMapper.findByHostName(host);
		if (duplicate != null) {
			throw new IllegalArgumentException("이미 등록된 도메인입니다: " + host);
		}
		String resolvedActor = actor != null && !actor.isBlank() ? actor : "SYSTEM";
		BizCompanyDomain domain = new BizCompanyDomain();
		domain.setCompanyId(companyId);
		domain.setHostName(host);
		domain.setPrimaryYn("Y");
		domain.setSslYn("N");
		domain.setVerifyStatusCd("VERIFIED");
		domain.setUseYn("Y");
		domain.setRegId(resolvedActor);
		domain.setUpdateId(resolvedActor);
		bizCompanyDomainMapper.insert(domain);
	}

	@Transactional
	public Long save(BizCompanyDomainFormDto dto, HttpSession session) {
		validate(dto);
		String actor = resolveActor(session);
		BizCompanyDomain entity = toEntity(dto);
		entity.setRegId(actor);
		entity.setUpdateId(actor);

		BizCompanyDomain duplicate = bizCompanyDomainMapper.findByHostName(entity.getHostName());
		if (duplicate != null
				&& (dto.getDomainId() == null || !duplicate.getDomainId().equals(dto.getDomainId()))) {
			throw new IllegalArgumentException("이미 등록된 도메인입니다: " + entity.getHostName());
		}

		if ("Y".equalsIgnoreCase(entity.getPrimaryYn())) {
			bizCompanyDomainMapper.clearPrimaryForCompany(entity.getCompanyId());
		}

		if (dto.getDomainId() == null) {
			bizCompanyDomainMapper.insert(entity);
			return entity.getDomainId();
		}
		BizCompanyDomain existing = bizCompanyDomainMapper.findById(dto.getDomainId());
		if (existing == null) {
			throw new IllegalArgumentException("도메인을 찾을 수 없습니다.");
		}
		entity.setDomainId(dto.getDomainId());
		bizCompanyDomainMapper.update(entity);
		return entity.getDomainId();
	}

	@Transactional
	public void delete(Long domainId) {
		deleteMany(List.of(domainId));
	}

	@Transactional
	public int deleteMany(List<Long> domainIds) {
		if (domainIds == null || domainIds.isEmpty()) {
			throw new IllegalArgumentException("삭제할 도메인을 선택하세요.");
		}
		int deleted = 0;
		for (Long domainId : domainIds) {
			if (domainId == null) {
				continue;
			}
			if (bizCompanyDomainMapper.findById(domainId) == null) {
				continue;
			}
			bizCompanyDomainMapper.deleteById(domainId);
			deleted++;
		}
		if (deleted == 0) {
			throw new IllegalArgumentException("삭제할 도메인을 찾을 수 없습니다.");
		}
		return deleted;
	}

	private void validate(BizCompanyDomainFormDto dto) {
		if (dto == null || dto.getCompanyId() == null) {
			throw new IllegalArgumentException("업체는 필수입니다.");
		}
		if (bizCompanyMapper.findById(dto.getCompanyId()) == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다.");
		}
		String host = normalizeHostName(dto.getHostName());
		if (host == null || host.isBlank()) {
			throw new IllegalArgumentException("도메인(호스트명)은 필수입니다.");
		}
		if (host.length() > 253) {
			throw new IllegalArgumentException("도메인은 253자 이하여야 합니다.");
		}
		if (!HOST_PATTERN.matcher(host).matches()) {
			throw new IllegalArgumentException("올바른 도메인 형식이 아닙니다. (예: shop.example.com)");
		}
		dto.setHostName(host);
		if (isBlank(dto.getSslYn())) {
			dto.setSslYn("Y");
		}
		if (isBlank(dto.getPrimaryYn())) {
			dto.setPrimaryYn("N");
		}
		if (isBlank(dto.getVerifyStatusCd())) {
			dto.setVerifyStatusCd("PENDING");
		}
		if (isBlank(dto.getUseYn())) {
			dto.setUseYn("Y");
		}
		if ("Y".equalsIgnoreCase(dto.getSslYn())) {
			LocalDateTime notAfter = AppDateTimeFormats.parseDateTime(dto.getSslCertNotAfter());
			LocalDateTime notBefore = AppDateTimeFormats.parseDateTime(dto.getSslCertNotBefore());
			if (notAfter == null) {
				throw new IllegalArgumentException("HTTPS 사용 시 SSL 인증서를 등록하고 유효기간을 확인하세요.");
			}
			if (notBefore != null && notAfter.isBefore(notBefore)) {
				throw new IllegalArgumentException("SSL 인증서 만료일이 시작일보다 앞설 수 없습니다.");
			}
		}
	}

	static String normalizeHostName(String raw) {
		if (raw == null) {
			return null;
		}
		String host = raw.trim().toLowerCase();
		if (host.startsWith("https://")) {
			host = host.substring(8);
		} else if (host.startsWith("http://")) {
			host = host.substring(7);
		}
		int slash = host.indexOf('/');
		if (slash >= 0) {
			host = host.substring(0, slash);
		}
		int colon = host.indexOf(':');
		if (colon >= 0) {
			host = host.substring(0, colon);
		}
		if (host.startsWith("www.")) {
			host = host.substring(4);
		}
		return host.isBlank() ? null : host;
	}

	private BizCompanyDomain toEntity(BizCompanyDomainFormDto dto) {
		BizCompanyDomain d = new BizCompanyDomain();
		d.setCompanyId(dto.getCompanyId());
		d.setHostName(dto.getHostName());
		d.setPrimaryYn("Y".equalsIgnoreCase(dto.getPrimaryYn()) ? "Y" : "N");
		d.setSslYn("N".equalsIgnoreCase(dto.getSslYn()) ? "N" : "Y");
		if ("N".equalsIgnoreCase(d.getSslYn())) {
			d.setSslCertNotBefore(null);
			d.setSslCertNotAfter(null);
			d.setSslCertSubject(null);
			d.setSslCertIssuer(null);
			d.setSslCertFileId(null);
		} else {
			d.setSslCertNotBefore(AppDateTimeFormats.parseDateTime(dto.getSslCertNotBefore()));
			d.setSslCertNotAfter(AppDateTimeFormats.parseDateTime(dto.getSslCertNotAfter()));
			d.setSslCertSubject(trimToNull(dto.getSslCertSubject()));
			d.setSslCertIssuer(trimToNull(dto.getSslCertIssuer()));
			d.setSslCertFileId(dto.getSslCertFileId());
		}
		d.setVerifyStatusCd(dto.getVerifyStatusCd().trim().toUpperCase());
		d.setUseYn("N".equalsIgnoreCase(dto.getUseYn()) ? "N" : "Y");
		d.setMemo(trimToNull(dto.getMemo()));
		return d;
	}

	private Map<String, Object> toDto(BizCompanyDomain row) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("domainId", row.getDomainId());
		map.put("companyId", row.getCompanyId());
		map.put("companyNm", row.getCompanyNm());
		map.put("hostName", row.getHostName());
		map.put("primaryYn", row.getPrimaryYn());
		map.put("sslYn", row.getSslYn());
		map.put("sslCertNotBefore", AppDateTimeFormats.formatDateTime(row.getSslCertNotBefore()));
		map.put("sslCertNotAfter", AppDateTimeFormats.formatDateTime(row.getSslCertNotAfter()));
		map.put("sslCertSubject", row.getSslCertSubject());
		map.put("sslCertIssuer", row.getSslCertIssuer());
		map.put("sslCertFileId", row.getSslCertFileId());
		appendSslValidityFlags(map, row.getSslCertNotAfter());
		map.put("verifyStatusCd", row.getVerifyStatusCd());
		map.put("useYn", row.getUseYn());
		map.put("memo", row.getMemo());
		map.put("accessUrl", buildAccessUrl(row));
		map.put("customerStoreUrl", buildCustomerStoreUrl(row));
		map.put("regDt", AppDateTimeFormats.formatDateTime(row.getRegDt()));
		map.put("updateDt", AppDateTimeFormats.formatDateTime(row.getUpdateDt()));
		return map;
	}

	private static String buildAccessUrl(BizCompanyDomain row) {
		String scheme = "Y".equalsIgnoreCase(row.getSslYn()) ? "https" : "http";
		return scheme + "://" + row.getHostName() + "/";
	}

	private String buildCustomerStoreUrl(BizCompanyDomain row) {
		String base = buildAccessUrl(row);
		if (row.getHostName() == null || row.getHostName().isBlank()) {
			return base;
		}
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		String path = crypto != null && crypto.isEnabled()
				? crypto.toPublicPath("/shop-home")
				: "/shop-home";
		if (base.endsWith("/")) {
			return base + (path.startsWith("/") ? path.substring(1) : path);
		}
		return base + path;
	}

	private String resolveActor(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		return userId != null && !userId.isBlank() ? userId : "SYSTEM";
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static void appendSslValidityFlags(Map<String, Object> map, LocalDateTime notAfter) {
		if (notAfter == null) {
			map.put("sslCertExpired", false);
			map.put("sslCertExpiringSoon", false);
			map.put("sslCertDaysRemaining", null);
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		long daysRemaining = ChronoUnit.DAYS.between(now.toLocalDate(), notAfter.toLocalDate());
		map.put("sslCertDaysRemaining", daysRemaining);
		map.put("sslCertExpired", notAfter.isBefore(now));
		map.put("sslCertExpiringSoon", !notAfter.isBefore(now) && daysRemaining <= SSL_EXPIRING_SOON_DAYS);
	}
}
