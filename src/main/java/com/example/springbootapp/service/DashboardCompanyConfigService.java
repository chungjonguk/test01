package com.example.springbootapp.service;

import com.example.springbootapp.config.DashboardWidgetCatalog;
import com.example.springbootapp.domain.DashboardCompanyConfig;
import com.example.springbootapp.mapper.BizCompanyMapper;
import com.example.springbootapp.mapper.DashboardCompanyConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardCompanyConfigService {

	private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
	};

	private final DashboardCompanyConfigMapper configMapper;
	private final BizCompanyMapper bizCompanyMapper;
	private final ObjectMapper objectMapper;

	public DashboardCompanyConfigService(
			DashboardCompanyConfigMapper configMapper,
			BizCompanyMapper bizCompanyMapper,
			ObjectMapper objectMapper) {
		this.configMapper = configMapper;
		this.bizCompanyMapper = bizCompanyMapper;
		this.objectMapper = objectMapper;
	}

	public Map<String, Object> getConfig(Long companyId) {
		ensureCompanyExists(companyId);
		List<String> hidden = loadHidden(companyId);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("companyId", companyId);
		body.put("hidden", hidden);
		body.put("order", autoOrder(hidden));
		return body;
	}

	@Transactional
	public void saveConfig(Long companyId, List<String> hidden, String userId) {
		ensureCompanyExists(companyId);
		List<String> normalized = normalizeHidden(hidden);
		String json = writeJson(normalized);
		String actor = userId != null && !userId.isBlank() ? userId : "SYSTEM";

		DashboardCompanyConfig existing = configMapper.findByCompanyId(companyId);
		if (existing == null) {
			DashboardCompanyConfig row = new DashboardCompanyConfig();
			row.setCompanyId(companyId);
			row.setHiddenJson(json);
			row.setRegId(actor);
			row.setUpdateId(actor);
			configMapper.insert(row);
		} else {
			existing.setHiddenJson(json);
			existing.setUpdateId(actor);
			configMapper.update(existing);
		}
	}

	public List<String> defaultHidden() {
		List<String> allIds = DashboardWidgetCatalog.all().stream().map(w -> w.id()).toList();
		List<String> defaultVisible = DashboardWidgetCatalog.defaultEnabledIds();
		List<String> hidden = new ArrayList<>();
		for (String id : allIds) {
			if (!defaultVisible.contains(id)) {
				hidden.add(id);
			}
		}
		return hidden;
	}

	private List<String> loadHidden(Long companyId) {
		DashboardCompanyConfig row = configMapper.findByCompanyId(companyId);
		if (row == null || row.getHiddenJson() == null || row.getHiddenJson().isBlank()) {
			return defaultHidden();
		}
		try {
			List<String> parsed = objectMapper.readValue(row.getHiddenJson(), STRING_LIST);
			return normalizeHidden(parsed);
		} catch (Exception ex) {
			return defaultHidden();
		}
	}

	private List<String> autoOrder(List<String> hidden) {
		List<String> hiddenSet = normalizeHidden(hidden);
		List<String> order = new ArrayList<>();
		var widgets = DashboardWidgetCatalog.all();
		Map<String, List<String>> buckets = new LinkedHashMap<>();
		buckets.put("compact", new ArrayList<>());
		buckets.put("half", new ArrayList<>());
		buckets.put("wide", new ArrayList<>());
		buckets.put("full", new ArrayList<>());
		for (var w : widgets) {
			if (hiddenSet.contains(w.id())) {
				continue;
			}
			String type = bandType(w.colClass());
			buckets.get(type).add(w.id());
		}
		order.addAll(buckets.get("compact"));
		order.addAll(buckets.get("half"));
		order.addAll(buckets.get("wide"));
		order.addAll(buckets.get("full"));
		for (var w : widgets) {
			if (hiddenSet.contains(w.id()) && !order.contains(w.id())) {
				order.add(w.id());
			}
		}
		return order;
	}

	private static String bandType(String colClass) {
		if (colClass != null && colClass.contains("col-12") && !colClass.matches(".*col-(sm|md|lg|xl|xxl)-.*")) {
			return "full";
		}
		if (colClass != null && (colClass.contains("col-lg-7") || colClass.contains("col-xxl-6")
				|| colClass.contains("col-xl-8"))) {
			return "wide";
		}
		if (colClass != null && (colClass.contains("col-lg-6") || colClass.contains("col-lg-5"))) {
			return "half";
		}
		return "compact";
	}

	private List<String> normalizeHidden(List<String> hidden) {
		List<String> all = DashboardWidgetCatalog.all().stream().map(w -> w.id()).toList();
		List<String> out = new ArrayList<>();
		if (hidden != null) {
			for (String id : hidden) {
				if (id != null && !id.isBlank() && all.contains(id) && !out.contains(id)) {
					out.add(id);
				}
			}
		}
		return out;
	}

	private String writeJson(List<String> hidden) {
		try {
			return objectMapper.writeValueAsString(hidden);
		} catch (Exception ex) {
			throw new IllegalStateException("hidden JSON 직렬화 실패", ex);
		}
	}

	private void ensureCompanyExists(Long companyId) {
		if (companyId == null) {
			throw new IllegalArgumentException("업체 ID가 필요합니다.");
		}
		if (bizCompanyMapper.findById(companyId) == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다. ID=" + companyId);
		}
	}
}
