package com.example.springbootapp.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.domain.CommonCode;
import com.example.springbootapp.domain.CommonCodeValue;
import com.example.springbootapp.mapper.CommonCodeMapper;
import com.example.springbootapp.mapper.CommonCodeValueMapper;

@Service
@Transactional(readOnly = true)
public class CommonCodeService {

	private final CommonCodeMapper commonCodeMapper;
	private final CommonCodeValueMapper commonCodeValueMapper;

	public CommonCodeService(CommonCodeMapper commonCodeMapper, CommonCodeValueMapper commonCodeValueMapper) {
		this.commonCodeMapper = commonCodeMapper;
		this.commonCodeValueMapper = commonCodeValueMapper;
	}

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
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("codeId", group.getCodeId());
			row.put("codeNm", group.getCodeNm());
			row.put("useYn", group.getUseYn() != null ? group.getUseYn() : "Y");
			row.put("regId", group.getRegId());
			row.put("regdateDt", group.getRegdateDt());
			row.put("updateId", group.getUpdateId());
			row.put("updateDt", group.getUpdateDt());
			row.put("codes", valuesByCodeId.getOrDefault(group.getCodeId(), List.of()));
			result.add(row);
		}
		return result;
	}

	private Map<String, Object> toDetailMap(CommonCodeValue value, int sort) {
		Map<String, Object> detail = new LinkedHashMap<>();
		String codeVal = value.getCodeVal() != null ? value.getCodeVal() : "";
		detail.put("code", codeVal);
		detail.put("name", codeVal);
		detail.put("sort", sort);
		detail.put("useYn", value.getUseYn() != null ? value.getUseYn() : "Y");
		detail.put("regId", value.getRegId());
		detail.put("regdateDt", value.getRegdateDt());
		detail.put("updateId", value.getUpdateId());
		detail.put("updateDt", value.getUpdateDt());
		return detail;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
