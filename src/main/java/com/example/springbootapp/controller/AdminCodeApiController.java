package com.example.springbootapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.service.CommonCodeService;

@RestController
@RequestMapping("/api/admin/codes")
public class AdminCodeApiController {

	private final CommonCodeService commonCodeService;

	public AdminCodeApiController(CommonCodeService commonCodeService) {
		this.commonCodeService = commonCodeService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String codeId,
			@RequestParam(required = false) String codeNm,
			@RequestParam(required = false) String useYn) {
		List<Map<String, Object>> groups = commonCodeService.searchGroups(codeId, codeNm, useYn);
		Map<String, Object> body = new HashMap<>();
		body.put("groups", groups);
		body.put("count", groups.size());
		return ResponseEntity.ok(body);
	}
}
