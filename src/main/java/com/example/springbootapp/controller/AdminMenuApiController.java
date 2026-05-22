package com.example.springbootapp.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.config.ScreenSidebarLoader;
import com.example.springbootapp.config.ScreenTableResolver;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.domain.ScreenTableMap;
import com.example.springbootapp.service.ScreenListService;
import com.example.springbootapp.service.ScreenTableMapService;

@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuApiController {

	private final ScreenListService screenListService;
	private final ScreenTableMapService screenTableMapService;

	public AdminMenuApiController(
			ScreenListService screenListService,
			ScreenTableMapService screenTableMapService) {
		this.screenListService = screenListService;
		this.screenTableMapService = screenTableMapService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String screenId,
			@RequestParam(required = false) String screenNm,
			@RequestParam(required = false) String uriPath,
			@RequestParam(required = false) String useYn) {
		Map<String, ScreenTableMap> tableByUri = screenTableMapService.findAllByUri();
		List<Map<String, Object>> screens = screenListService.searchForAdmin(screenId, screenNm, uriPath, useYn)
				.stream()
				.map(screen -> toDto(screen, tableByUri.get(screen.getUriPath())))
				.collect(Collectors.toList());
		Map<String, Object> body = new HashMap<>();
		body.put("screens", screens);
		body.put("count", screens.size());
		return ResponseEntity.ok(body);
	}

	private Map<String, Object> toDto(ScreenList screen, ScreenTableMap mapped) {
		ScreenTableResolver.Mapping fallback = ScreenTableResolver.resolve(screen.getUriPath());
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("screenId", screen.getScreenId());
		row.put("screenNm", ScreenSidebarLoader.resolveDisplayName(screen.getScreenNm(), screen.getUriPath()));
		row.put("uriPath", screen.getUriPath());
		row.put("templatePath", screen.getTemplatePath());
		row.put("sortOrd", screen.getSortOrd());
		row.put("useYn", screen.getUseYn());
		row.put("primaryTable", mapped != null ? mapped.getPrimaryTable() : fallback.primaryTable());
		row.put("relatedTables", mapped != null ? mapped.getRelatedTables() : fallback.relatedTables());
		row.put("dataType", mapped != null ? mapped.getDataType() : String.valueOf(fallback.dataType()));
		row.put("tableDesc", mapped != null ? mapped.getTableDesc() : fallback.tableDesc());
		row.put("regId", screen.getRegId());
		row.put("regDt", screen.getRegDt());
		row.put("updateId", screen.getUpdateId());
		row.put("updateDt", screen.getUpdateDt());
		return row;
	}
}
