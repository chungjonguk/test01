package com.example.springbootapp.service;

import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.CompanyPageImageCatalog;
import com.example.springbootapp.domain.BizCompanyPageImage;
import com.example.springbootapp.mapper.BizCompanyMapper;
import com.example.springbootapp.mapper.BizCompanyPageImageMapper;
import com.example.springbootapp.storage.NasMediaType;
import com.example.springbootapp.storage.NasStorageService;
import com.example.springbootapp.storage.NasStorageService.NasStoredFile;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BizCompanyPageImageService {

	private final BizCompanyPageImageMapper pageImageMapper;
	private final BizCompanyMapper bizCompanyMapper;
	private final NasStorageService nasStorageService;
	private final SessionAuthService sessionAuthService;

	public BizCompanyPageImageService(
			BizCompanyPageImageMapper pageImageMapper,
			BizCompanyMapper bizCompanyMapper,
			NasStorageService nasStorageService,
			SessionAuthService sessionAuthService) {
		this.pageImageMapper = pageImageMapper;
		this.bizCompanyMapper = bizCompanyMapper;
		this.nasStorageService = nasStorageService;
		this.sessionAuthService = sessionAuthService;
	}

	public List<Map<String, Object>> listWithSlots(Long companyId) {
		requireCompany(companyId);
		Map<String, BizCompanyPageImage> byPage = new LinkedHashMap<>();
		for (BizCompanyPageImage row : pageImageMapper.listByCompanyId(companyId)) {
			byPage.put(row.getPageCd(), row);
		}
		List<Map<String, Object>> result = new ArrayList<>();
		for (CompanyPageImageCatalog.Slot slot : CompanyPageImageCatalog.all()) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("pageCd", slot.pageCd());
			item.put("label", slot.label());
			item.put("description", slot.description());
			item.put("hint", slot.hint());
			BizCompanyPageImage saved = byPage.get(slot.pageCd());
			if (saved != null) {
				item.put("image", toDto(saved));
			}
			result.add(item);
		}
		return result;
	}

	@Transactional
	public Map<String, Object> upload(
			Long companyId,
			String pageCd,
			String altText,
			MultipartFile file,
			HttpSession session) throws IOException {
		requireCompany(companyId);
		CompanyPageImageCatalog.Slot slot = CompanyPageImageCatalog.find(pageCd)
				.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 페이지 슬롯입니다: " + pageCd));
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("이미지 파일을 선택하세요.");
		}
		String regId = resolveRegId(session);
		NasStoredFile stored = nasStorageService.store(NasMediaType.COMPANY_PAGE, file, regId);
		String normalizedPageCd = slot.pageCd();
		BizCompanyPageImage existing = pageImageMapper.findByCompanyAndPageCd(companyId, normalizedPageCd);
		if (existing == null) {
			BizCompanyPageImage row = new BizCompanyPageImage();
			row.setCompanyId(companyId);
			row.setPageCd(normalizedPageCd);
			row.setNasFileId(stored.fileId());
			row.setUrlPath(stored.url());
			row.setAltText(trimAlt(altText));
			row.setUseYn("Y");
			row.setRegId(regId);
			row.setUpdateId(regId);
			pageImageMapper.insert(row);
			BizCompanyPageImage saved = pageImageMapper.findById(row.getImageId());
			return toDto(saved != null ? saved : row);
		}
		existing.setNasFileId(stored.fileId());
		existing.setUrlPath(stored.url());
		if (altText != null) {
			existing.setAltText(trimAlt(altText));
		}
		existing.setUseYn("Y");
		existing.setUpdateId(regId);
		pageImageMapper.update(existing);
		return toDto(existing);
	}

	@Transactional
	public void delete(Long imageId) {
		if (imageId == null) {
			throw new IllegalArgumentException("imageId가 필요합니다.");
		}
		BizCompanyPageImage row = pageImageMapper.findById(imageId);
		if (row == null) {
			throw new IllegalArgumentException("등록된 이미지가 없습니다.");
		}
		pageImageMapper.deleteById(imageId);
	}

	private void requireCompany(Long companyId) {
		if (companyId == null) {
			throw new IllegalArgumentException("업체를 선택하세요.");
		}
		if (bizCompanyMapper.findById(companyId) == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다.");
		}
	}

	private String resolveRegId(HttpSession session) {
		String regId = sessionAuthService.getLoginUserId(session);
		return regId != null && !regId.isBlank() ? regId.trim() : "SYSTEM";
	}

	private static String trimAlt(String altText) {
		if (altText == null) {
			return null;
		}
		String trimmed = altText.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Map<String, Object> toDto(BizCompanyPageImage row) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("imageId", row.getImageId());
		dto.put("companyId", row.getCompanyId());
		dto.put("pageCd", row.getPageCd());
		dto.put("nasFileId", row.getNasFileId());
		dto.put("url", row.getUrlPath());
		dto.put("altText", row.getAltText());
		dto.put("useYn", row.getUseYn());
		dto.put("regId", row.getRegId());
		dto.put("regDt", row.getRegDt());
		dto.put("updateId", row.getUpdateId());
		dto.put("updateDt", row.getUpdateDt());
		return dto;
	}
}
