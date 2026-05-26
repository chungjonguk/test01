package com.example.springbootapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.service.TableSequenceService;

/**
 * 관리자 테이블 시퀀스 REST API.
 * <p>기본 경로: {@code /api/admin/table-sequences}</p>
 */
@RestController
@RequestMapping("/api/admin/table-sequences")
public class AdminTableSequenceApiController {

	private final TableSequenceService tableSequenceService;

	public AdminTableSequenceApiController(TableSequenceService tableSequenceService) {
		this.tableSequenceService = tableSequenceService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String seqName,
			@RequestParam(required = false) String tableName,
			@RequestParam(required = false) String useYn) {
		List<Map<String, Object>> sequences = tableSequenceService.searchForAdmin(seqName, tableName, useYn);
		Map<String, Object> body = new HashMap<>();
		body.put("sequences", sequences);
		body.put("count", sequences.size());
		return ResponseEntity.ok(body);
	}

	@PostMapping("/sync")
	public ResponseEntity<Map<String, Object>> syncFromCatalog() {
		return ResponseEntity.ok(tableSequenceService.refreshFromCatalog());
	}
}
