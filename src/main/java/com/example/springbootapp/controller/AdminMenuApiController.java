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
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.service.ScreenListService;

@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuApiController {

	private final ScreenListService screenListService;

	public AdminMenuApiController(ScreenListService screenListService) {
		this.screenListService = screenListService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String screenId,
			@RequestParam(required = false) String screenNm,
			@RequestParam(required = false) String uriPath,
			@RequestParam(required = false) String useYn) {
		List<Map<String, Object>> screens = screenListService.searchForAdmin(screenId, screenNm, uriPath, useYn)
				.stream()
				.map(this::toDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new HashMap<>();
		body.put("screens", screens);
		body.put("count", screens.size());
		return ResponseEntity.ok(body);
	}

	private Map<String, Object> toDto(ScreenList screen) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("screenId", screen.getScreenId());
		row.put("screenNm", ScreenSidebarLoader.resolveDisplayName(screen.getScreenNm(), screen.getUriPath()));
		row.put("uriPath", screen.getUriPath());
		row.put("templatePath", screen.getTemplatePath());
		row.put("sortOrd", screen.getSortOrd());
		row.put("useYn", screen.getUseYn());
		row.put("regId", screen.getRegId());
		row.put("regDt", screen.getRegDt());
		row.put("updateId", screen.getUpdateId());
		row.put("updateDt", screen.getUpdateDt());
		return row;
	}
}
