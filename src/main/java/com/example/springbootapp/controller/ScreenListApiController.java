package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.service.ScreenListService;

@RestController
@RequestMapping("/api/screens")
public class ScreenListApiController {

	private final ScreenListService screenListService;

	public ScreenListApiController(ScreenListService screenListService) {
		this.screenListService = screenListService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list() {
		List<Map<String, Object>> rows = screenListService.findAllActive().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("count", rows.size());
		body.put("screens", rows);
		return ResponseEntity.ok(body);
	}

	@GetMapping("/{screenId}")
	public ResponseEntity<Map<String, Object>> one(@PathVariable String screenId) {
		ScreenList screen = screenListService.findByScreenId(screenId);
		if (screen == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(toDto(screen));
	}

	private Map<String, Object> toDto(ScreenList screen) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("screenId", screen.getScreenId());
		row.put("screenNm", screen.getScreenNm());
		row.put("uriPath", screen.getUriPath());
		row.put("templatePath", screen.getTemplatePath());
		row.put("sortOrd", screen.getSortOrd());
		row.put("useYn", screen.getUseYn());
		return row;
	}
}
