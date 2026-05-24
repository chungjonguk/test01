package com.example.springbootapp.service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.BizCompany;
import com.example.springbootapp.dto.BizCompanyFormDto;
import com.example.springbootapp.mapper.BizCompanyMapper;
import jakarta.servlet.http.HttpSession;
/**
 * 업체(비즈니스 회사) 정보 검색·조회·등록·수정·삭제를 처리하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class BizCompanyService {
	private static final int MAX_LIMIT = 500;
	private final BizCompanyMapper bizCompanyMapper;
	private final SessionAuthService sessionAuthService;
	public BizCompanyService(BizCompanyMapper bizCompanyMapper, SessionAuthService sessionAuthService) {
		this.bizCompanyMapper = bizCompanyMapper;
		this.sessionAuthService = sessionAuthService;
	}
	/**
	 * 조건에 맞는 업체 목록을 검색한다.
	 *
	 * @param companyNm 업체명 (부분 일치, null 허용)
	 * @param bizNo     사업자등록번호 (부분 일치, null 허용)
	 * @param statusCd  상태 코드 (null 허용)
	 * @param useYn     사용 여부 Y/N (null 허용)
	 * @param limit     최대 조회 건수 (1~500으로 보정)
	 * @return 업체 정보 맵 목록
	 */
	public List<Map<String, Object>> search(String companyNm, String bizNo, String statusCd, String useYn, int limit) {
		int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
		return bizCompanyMapper.search(trimToNull(companyNm), trimToNull(bizNo), trimToNull(statusCd), trimToNull(useYn),
				safeLimit).stream().map(this::toDto).collect(Collectors.toList());
	}
	/**
	 * 업체 ID로 상세 정보를 조회한다.
	 *
	 * @param companyId 업체 ID
	 * @return 업체 정보 맵, 없으면 null
	 */
	public Map<String, Object> findById(Long companyId) {
		BizCompany company = bizCompanyMapper.findById(companyId);
		if (company == null) {
			return null;
		}
		return toDto(company);
	}
	/**
	 * 업체를 신규 등록하거나 기존 업체를 수정한다.
	 *
	 * @param dto     업체 입력 폼
	 * @param session HTTP 세션 (등록·수정자 ID 추출용)
	 * @return 저장된 업체 ID
	 */
	@Transactional
	public Long save(BizCompanyFormDto dto, HttpSession session) {
		validate(dto);
		String actor = resolveActor(session);
		BizCompany entity = toEntity(dto);
		entity.setRegId(actor);
		entity.setUpdateId(actor);
		if (dto.getCompanyId() == null) {
			bizCompanyMapper.insert(entity);
			return entity.getCompanyId();
		}
		BizCompany existing = bizCompanyMapper.findById(dto.getCompanyId());
		if (existing == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다. ID=" + dto.getCompanyId());
		}
		entity.setCompanyId(dto.getCompanyId());
		bizCompanyMapper.update(entity);
		return entity.getCompanyId();
	}
	/**
	 * 업체를 삭제한다.
	 *
	 * @param companyId 삭제할 업체 ID
	 */
	@Transactional
	public void delete(Long companyId) {
		if (companyId == null) {
			throw new IllegalArgumentException("업체 ID가 필요합니다.");
		}
		if (bizCompanyMapper.findById(companyId) == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다. ID=" + companyId);
		}
		bizCompanyMapper.deleteById(companyId);
	}
	private void validate(BizCompanyFormDto dto) {
		if (dto == null || isBlank(dto.getCompanyNm())) {
			throw new IllegalArgumentException("업체명은 필수입니다.");
		}
		if (isBlank(dto.getStatusCd())) {
			dto.setStatusCd("ACTIVE");
		}
		if (isBlank(dto.getUseYn())) {
			dto.setUseYn("Y");
		}
		normalizeBizNo(dto);
	}
	private void normalizeBizNo(BizCompanyFormDto dto) {
		String bizNo = trimToNull(dto.getBizNo());
		if (bizNo == null) {
			return;
		}
		String digits = bizNo.replaceAll("\\D", "");
		if (digits.length() != 10) {
			throw new IllegalArgumentException("사업자등록번호는 10자리 숫자(000-00-00000) 형식이어야 합니다.");
		}
		dto.setBizNo(String.format("%s-%s-%s", digits.substring(0, 3), digits.substring(3, 5), digits.substring(5)));
	}
	private BizCompany toEntity(BizCompanyFormDto dto) {
		BizCompany c = new BizCompany();
		c.setCompanyNm(dto.getCompanyNm().trim());
		c.setBizNo(trimToNull(dto.getBizNo()));
		c.setCeoNm(trimToNull(dto.getCeoNm()));
		c.setTel(trimToNull(dto.getTel()));
		c.setEmail(trimToNull(dto.getEmail()));
		c.setAddress(trimToNull(dto.getAddress()));
		c.setStatusCd(dto.getStatusCd().trim());
		c.setUseYn("N".equalsIgnoreCase(dto.getUseYn()) ? "N" : "Y");
		c.setMemo(trimToNull(dto.getMemo()));
		return c;
	}
	private Map<String, Object> toDto(BizCompany c) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("companyId", c.getCompanyId());
		row.put("companyNm", c.getCompanyNm());
		row.put("bizNo", c.getBizNo());
		row.put("ceoNm", c.getCeoNm());
		row.put("tel", c.getTel());
		row.put("email", c.getEmail());
		row.put("address", c.getAddress());
		row.put("statusCd", c.getStatusCd());
		row.put("useYn", c.getUseYn());
		row.put("memo", c.getMemo());
		row.put("regId", c.getRegId());
		row.put("regDt", c.getRegDt());
		row.put("updateId", c.getUpdateId());
		row.put("updateDt", c.getUpdateDt());
		return row;
	}
	private String resolveActor(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		return userId != null && !userId.isBlank() ? userId : "SYSTEM";
	}
	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		return t.isEmpty() ? null : t;
	}
	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
