package com.example.springbootapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.dto.CodeGroupSaveRequest;
import com.example.springbootapp.service.CommonCodeService;

import jakarta.servlet.http.HttpSession;

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

	@PostMapping("/save")
	public ResponseEntity<Map<String, Object>> save(
			@RequestBody CodeGroupSaveRequest request,
			HttpSession session) {
		var result = commonCodeService.saveGroups(request, session);
		Map<String, Object> body = new HashMap<>();
		body.put("savedGroups", result.getSavedGroups());
		body.put("savedDetails", result.getSavedDetails());
		body.put("message", result.getMessage());
		return ResponseEntity.ok(body);
	}

	@DeleteMapping("/groups")
	public ResponseEntity<Map<String, Object>> deleteGroups(@RequestParam List<String> codeIds) {
		var result = commonCodeService.deleteGroups(codeIds);
		Map<String, Object> body = new HashMap<>();
		body.put("deleted", result.getDeletedGroups());
		body.put("deletedGroups", result.getDeletedGroups());
		body.put("deletedDetails", result.getDeletedDetails());
		body.put("message", result.getMessage());
		return ResponseEntity.ok(body);
	}

	@DeleteMapping("/{codeId}/values")
	public ResponseEntity<Map<String, Object>> deleteCodeValues(
			@PathVariable String codeId,
			@RequestParam List<String> codeVals) {
		int deleted = commonCodeService.deleteCodeValues(codeId, codeVals);
		Map<String, Object> body = new HashMap<>();
		body.put("deleted", deleted);
		body.put("codeId", codeId);
		body.put("message", deleted + "건의 상세코드가 삭제되었습니다.");
		return ResponseEntity.ok(body);
	}
}
