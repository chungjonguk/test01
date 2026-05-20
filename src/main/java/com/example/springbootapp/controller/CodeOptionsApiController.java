package com.example.springbootapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.dto.CodeOption;
import com.example.springbootapp.service.CommonCodeService;

@RestController
@RequestMapping("/api/codes")
public class CodeOptionsApiController {

	private final CommonCodeService commonCodeService;

	public CodeOptionsApiController(CommonCodeService commonCodeService) {
		this.commonCodeService = commonCodeService;
	}

	@GetMapping("/options")
	public ResponseEntity<Map<String, List<CodeOption>>> allOptions() {
		return ResponseEntity.ok(commonCodeService.findAllActiveOptionsMap());
	}

	@GetMapping("/options/{codeId}")
	public ResponseEntity<Map<String, Object>> optionsByGroup(@PathVariable String codeId) {
		List<CodeOption> options = commonCodeService.findActiveOptions(codeId);
		Map<String, Object> body = new HashMap<>();
		body.put("codeId", codeId);
		body.put("options", options);
		body.put("count", options.size());
		return ResponseEntity.ok(body);
	}
}
