package com.example.springbootapp.service;

import com.example.springbootapp.domain.BizCompany;
import com.example.springbootapp.mapper.BizCompanyMapper;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 대시보드 조회·구성 시 선택 업체(세션).
 */
@Service
public class DashboardCompanySessionService {

	public static final String SESSION_COMPANY_ID = "dashboardCompanyId";

	private final BizCompanyMapper bizCompanyMapper;

	public DashboardCompanySessionService(BizCompanyMapper bizCompanyMapper) {
		this.bizCompanyMapper = bizCompanyMapper;
	}

	/** 사용 중(use_yn=Y)인 전체 업체 — 상태(ACTIVE/INACTIVE) 무관 */
	public List<Map<String, Object>> listActiveCompanies() {
		List<BizCompany> rows = bizCompanyMapper.search(null, null, null, "Y", 500);
		List<Map<String, Object>> list = new ArrayList<>();
		for (BizCompany c : rows) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("companyId", c.getCompanyId());
			row.put("companyNm", c.getCompanyNm());
			row.put("statusCd", c.getStatusCd());
			row.put("bizNo", c.getBizNo());
			list.add(row);
		}
		return list;
	}

	public Long getSelectedCompanyId(HttpSession session) {
		if (session == null) {
			return null;
		}
		Object raw = session.getAttribute(SESSION_COMPANY_ID);
		if (raw instanceof Number number) {
			return number.longValue();
		}
		if (raw instanceof String text && !text.isBlank()) {
			try {
				return Long.parseLong(text.trim());
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}

	public void setSelectedCompanyId(HttpSession session, Long companyId) {
		if (session == null || companyId == null) {
			return;
		}
		session.setAttribute(SESSION_COMPANY_ID, companyId);
	}

	/**
	 * 세션 선택값이 없거나 유효하지 않으면 첫 번째 사용 업체로 설정한다.
	 */
	public Long resolveSelectedCompanyId(HttpSession session) {
		Long selected = getSelectedCompanyId(session);
		if (selected != null && bizCompanyMapper.findById(selected) != null) {
			return selected;
		}
		List<Map<String, Object>> companies = listActiveCompanies();
		if (companies.isEmpty()) {
			return null;
		}
		Long first = ((Number) companies.get(0).get("companyId")).longValue();
		setSelectedCompanyId(session, first);
		return first;
	}

	public String companyName(Long companyId) {
		if (companyId == null) {
			return null;
		}
		BizCompany company = bizCompanyMapper.findById(companyId);
		return company != null ? company.getCompanyNm() : null;
	}
}
