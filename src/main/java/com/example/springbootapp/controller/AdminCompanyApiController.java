package com.example.springbootapp.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

import com.example.springbootapp.dto.BizCompanyFormDto;
import com.example.springbootapp.service.BizCompanyService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin/companies")
public class AdminCompanyApiController {

	private final BizCompanyService bizCompanyService;

	public AdminCompanyApiController(BizCompanyService bizCompanyService) {
		this.bizCompanyService = bizCompanyService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String companyNm,
			@RequestParam(required = false) String bizNo,
			@RequestParam(required = false) String statusCd,
			@RequestParam(required = false) String useYn,
			@RequestParam(defaultValue = "200") int limit) {
		try {
			List<Map<String, Object>> companies = bizCompanyService.search(companyNm, bizNo, statusCd, useYn, limit);
			Map<String, Object> body = new HashMap<>();
			body.put("companies", companies);
			body.put("count", companies.size());
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@GetMapping("/{companyId}")
	public ResponseEntity<Map<String, Object>> findOne(@PathVariable Long companyId) {
		Map<String, Object> company = bizCompanyService.findById(companyId);
		if (company == null) {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", false);
			body.put("message", "업체를 찾을 수 없습니다.");
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> body = new HashMap<>();
		body.put("company", company);
		return ResponseEntity.ok(body);
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> save(@RequestBody BizCompanyFormDto dto, HttpSession session) {
		try {
			Long id = bizCompanyService.save(dto, session);
			Map<String, Object> body = new HashMap<>();
			body.put("success", true);
			body.put("companyId", id);
			body.put("message", dto.getCompanyId() == null ? "업체가 등록되었습니다." : "업체 정보가 수정되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@DeleteMapping("/{companyId}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long companyId) {
		try {
			bizCompanyService.delete(companyId);
			Map<String, Object> body = new HashMap<>();
			body.put("success", true);
			body.put("message", "업체가 삭제되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		body.put("companies", List.of());
		body.put("count", 0);
		return ResponseEntity.badRequest().body(body);
	}
}
