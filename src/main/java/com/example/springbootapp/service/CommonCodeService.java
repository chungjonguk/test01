package com.example.springbootapp.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.CommonCode;
import com.example.springbootapp.domain.CommonCodeValue;
import com.example.springbootapp.dto.CodeDetailSaveDto;
import com.example.springbootapp.dto.CodeGroupSaveDto;
import com.example.springbootapp.dto.CodeGroupSaveRequest;
import com.example.springbootapp.dto.CodeOption;
import com.example.springbootapp.mapper.CommonCodeMapper;
import com.example.springbootapp.mapper.CommonCodeValueMapper;
import jakarta.servlet.http.HttpSession;
/**
 * 공통 코드 그룹·상세 코드 조회·저장·삭제를 처리하는 서비스.
 */
@Service
public class CommonCodeService {
	private static final String DEFAULT_ACTOR = "SYSTEM";
	private final CommonCodeMapper commonCodeMapper;
	private final CommonCodeValueMapper commonCodeValueMapper;
	private final SessionAuthService sessionAuthService;
	public CommonCodeService(
			CommonCodeMapper commonCodeMapper,
			CommonCodeValueMapper commonCodeValueMapper,
			SessionAuthService sessionAuthService) {
		this.commonCodeMapper = commonCodeMapper;
		this.commonCodeValueMapper = commonCodeValueMapper;
		this.sessionAuthService = sessionAuthService;
	}
	/**
	 * 사용 중인 공통 코드 그룹의 선택 옵션 목록을 조회한다.
	 *
	 * @param codeId 코드 그룹 ID
	 * @return 활성 상세 코드 옵션 목록 (그룹 없음·미사용 시 빈 목록)
	 */
	@Transactional(readOnly = true)
	public List<CodeOption> findActiveOptions(String codeId) {
		String trimmedCodeId = trimToNull(codeId);
		if (trimmedCodeId == null) {
			return List.of();
		}
		CommonCode group = commonCodeMapper.findByCodeId(trimmedCodeId);
		if (group == null || !"Y".equalsIgnoreCase(group.getUseYn())) {
			return List.of();
		}
		List<CodeOption> options = new ArrayList<>();
		for (CommonCodeValue value : commonCodeValueMapper.findByCodeId(trimmedCodeId)) {
			if (!"Y".equalsIgnoreCase(value.getUseYn())) {
				continue;
			}
			options.add(toCodeOption(value));
		}
		return options;
	}
	/**
	 * 모든 활성 공통 코드 그룹의 선택 옵션을 코드 ID별 맵으로 조회한다.
	 *
	 * @return 코드 그룹 ID → 옵션 목록 맵
	 */
	@Transactional(readOnly = true)
	public Map<String, List<CodeOption>> findAllActiveOptionsMap() {
		List<CommonCode> groups = commonCodeMapper.search(null, null, "Y");
		if (groups.isEmpty()) {
			return Map.of();
		}
		List<String> codeIds = groups.stream().map(CommonCode::getCodeId).toList();
		Map<String, List<CodeOption>> result = new LinkedHashMap<>();
		for (String codeId : codeIds) {
			result.put(codeId, new ArrayList<>());
		}
		for (CommonCodeValue value : commonCodeValueMapper.findByCodeIds(codeIds)) {
			if (!"Y".equalsIgnoreCase(value.getUseYn())) {
				continue;
			}
			List<CodeOption> bucket = result.get(value.getCodeId());
			if (bucket != null) {
				bucket.add(toCodeOption(value));
			}
		}
		return result;
	}
	/**
	 * 조건에 맞는 코드 그룹과 하위 상세 코드를 검색한다.
	 *
	 * @param codeId 코드 그룹 ID (부분 일치, null 허용)
	 * @param codeNm 코드 그룹명 (부분 일치, null 허용)
	 * @param useYn  사용 여부 Y/N (null 허용)
	 * @return 그룹 정보와 상세 코드 목록(codes)을 포함한 맵 목록
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> searchGroups(String codeId, String codeNm, String useYn) {
		String trimmedCodeId = trimToNull(codeId);
		String trimmedCodeNm = trimToNull(codeNm);
		String trimmedUseYn = trimToNull(useYn);
		List<CommonCode> groups = commonCodeMapper.search(trimmedCodeId, trimmedCodeNm, trimmedUseYn);
		if (groups.isEmpty()) {
			return List.of();
		}
		List<String> codeIds = groups.stream().map(CommonCode::getCodeId).toList();
		Map<String, List<Map<String, Object>>> valuesByCodeId = new LinkedHashMap<>();
		for (String id : codeIds) {
			valuesByCodeId.put(id, new ArrayList<>());
		}
		List<CommonCodeValue> values = commonCodeValueMapper.findByCodeIds(codeIds);
		for (CommonCodeValue value : values) {
			List<Map<String, Object>> bucket = valuesByCodeId.get(value.getCodeId());
			if (bucket == null) {
				continue;
			}
			bucket.add(toDetailMap(value, bucket.size() + 1));
		}
		List<Map<String, Object>> result = new ArrayList<>();
		for (CommonCode group : groups) {
			List<Map<String, Object>> detailCodes = valuesByCodeId.getOrDefault(group.getCodeId(), List.of());
			Map<String, Object> row = toGroupMap(group, detailCodes.size());
			row.put("codes", detailCodes);
			result.add(row);
		}
		return result;
	}
	/**
	 * 코드 그룹과 소속 상세 코드를 일괄 삭제한다.
	 *
	 * @param codeIds 삭제할 코드 그룹 ID 목록
	 * @return 삭제된 그룹·상세 건수 결과
	 */
	@Transactional
	public CodeGroupDeleteResult deleteGroups(List<String> codeIds) {
		List<String> validIds = normalizeCodeIds(codeIds);
		if (validIds.isEmpty()) {
			return new CodeGroupDeleteResult(0, 0);
		}
		int deletedDetails = commonCodeValueMapper.deleteByCodeIds(validIds);
		int deletedGroups = commonCodeMapper.deleteByCodeIds(validIds);
		return new CodeGroupDeleteResult(deletedGroups, deletedDetails);
	}
	/**
	 * 코드 그룹과 상세 코드를 일괄 저장(등록·수정·동기화)한다.
	 *
	 * @param request 저장 요청 (그룹·상세 목록)
	 * @param session HTTP 세션 (등록·수정자 ID 추출용)
	 * @return 저장된 그룹·상세 건수 결과
	 */
	@Transactional
	public CodeSaveResult saveGroups(CodeGroupSaveRequest request, HttpSession session) {
		if (request == null || request.getGroups() == null || request.getGroups().isEmpty()) {
			return new CodeSaveResult(0, 0);
		}
		String actor = resolveActor(session);
		LocalDateTime now = LocalDateTime.now();
		int savedGroups = 0;
		int savedDetails = 0;
		for (CodeGroupSaveDto groupDto : request.getGroups()) {
			String codeId = trimToNull(groupDto.getCodeId());
			if (codeId == null) {
				continue;
			}
			String codeNm = trimToNull(groupDto.getCodeNm());
			if (codeNm == null) {
				codeNm = codeId;
			}
			String useYn = normalizeUseYn(groupDto.getUseYn());
			CommonCode group = new CommonCode();
			group.setCodeId(codeId);
			group.setCodeNm(codeNm);
			group.setUseYn(useYn);
			group.setUpdateId(actor);
			group.setUpdateDt(now);
			if (commonCodeMapper.findByCodeId(codeId) != null) {
				commonCodeMapper.update(group);
			} else {
				group.setRegId(actor);
				group.setRegdateDt(now);
				commonCodeMapper.insert(group);
			}
			savedGroups++;
			List<CommonCodeValue> existingValues = commonCodeValueMapper.findByCodeId(codeId);
			Set<String> incomingVals = new HashSet<>();
			List<CodeDetailSaveDto> detailDtos = groupDto.getCodes() != null ? groupDto.getCodes() : List.of();
			for (CodeDetailSaveDto detailDto : detailDtos) {
				String codeVal = trimToNull(detailDto.getCodeVal());
				if (codeVal == null) {
					continue;
				}
				incomingVals.add(codeVal);
				CommonCodeValue value = new CommonCodeValue();
				value.setCodeId(codeId);
				value.setCodeVal(codeVal);
				value.setUseYn(normalizeUseYn(detailDto.getUseYn()));
				value.setUpdateId(actor);
				value.setUpdateDt(now);
				if (commonCodeValueMapper.findByCodeIdAndCodeVal(codeId, codeVal) != null) {
					commonCodeValueMapper.update(value);
				} else {
					value.setRegId(actor);
					value.setRegdateDt(now);
					commonCodeValueMapper.insert(value);
				}
				savedDetails++;
			}
			for (CommonCodeValue existing : existingValues) {
				String existingVal = existing.getCodeVal();
				if (existingVal != null && !incomingVals.contains(existingVal)) {
					commonCodeValueMapper.deleteByCodeIdAndCodeVal(codeId, existingVal);
				}
			}
		}
		return new CodeSaveResult(savedGroups, savedDetails);
	}
	/**
	 * 특정 코드 그룹에서 지정한 상세 코드 값들을 삭제한다.
	 *
	 * @param codeId   코드 그룹 ID
	 * @param codeVals 삭제할 상세 코드 값 목록
	 * @return 삭제된 건수
	 */
	@Transactional
	public int deleteCodeValues(String codeId, List<String> codeVals) {
		String trimmedCodeId = trimToNull(codeId);
		if (trimmedCodeId == null || codeVals == null || codeVals.isEmpty()) {
			return 0;
		}
		int deleted = 0;
		for (String codeVal : codeVals) {
			String trimmedVal = trimToNull(codeVal);
			if (trimmedVal == null) {
				continue;
			}
			deleted += commonCodeValueMapper.deleteByCodeIdAndCodeVal(trimmedCodeId, trimmedVal);
		}
		return deleted;
	}
	private Map<String, Object> toGroupMap(CommonCode group, int detailCount) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("codeId", group.getCodeId());
		row.put("codeNm", group.getCodeNm());
		row.put("useYn", group.getUseYn() != null ? group.getUseYn() : "Y");
		row.put("regId", group.getRegId());
		row.put("regdateDt", group.getRegdateDt());
		row.put("updateId", group.getUpdateId());
		row.put("updateDt", group.getUpdateDt());
		row.put("detailCount", detailCount);
		return row;
	}
	private CodeOption toCodeOption(CommonCodeValue value) {
		String raw = value.getCodeVal() != null ? value.getCodeVal().trim() : "";
		if (raw.isEmpty()) {
			return new CodeOption("", "");
		}
		int separator = raw.indexOf('|');
		if (separator >= 0) {
			String val = raw.substring(0, separator).trim();
			String label = raw.substring(separator + 1).trim();
			if (label.isEmpty()) {
				label = val;
			}
			return new CodeOption(val, label);
		}
		return new CodeOption(raw, raw);
	}
	private Map<String, Object> toDetailMap(CommonCodeValue value, int sort) {
		Map<String, Object> detail = new LinkedHashMap<>();
		String codeVal = value.getCodeVal() != null ? value.getCodeVal() : "";
		detail.put("codeId", value.getCodeId());
		detail.put("codeVal", codeVal);
		detail.put("sort", sort);
		detail.put("useYn", value.getUseYn() != null ? value.getUseYn() : "Y");
		detail.put("regId", value.getRegId());
		detail.put("regdateDt", value.getRegdateDt());
		detail.put("updateId", value.getUpdateId());
		detail.put("updateDt", value.getUpdateDt());
		return detail;
	}
	private List<String> normalizeCodeIds(List<String> codeIds) {
		if (codeIds == null || codeIds.isEmpty()) {
			return List.of();
		}
		List<String> validIds = new ArrayList<>();
		for (String codeId : codeIds) {
			String trimmed = trimToNull(codeId);
			if (trimmed != null && !validIds.contains(trimmed)) {
				validIds.add(trimmed);
			}
		}
		return validIds;
	}
	private String resolveActor(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		if (userId != null && !userId.isBlank()) {
			return userId.trim();
		}
		return DEFAULT_ACTOR;
	}
	private String normalizeUseYn(String useYn) {
		return "N".equalsIgnoreCase(trimToNull(useYn)) ? "N" : "Y";
	}
	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
