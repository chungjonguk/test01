package com.example.springbootapp.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.springbootapp.dto.BizCompanyDomainBulkDeleteDto;
import com.example.springbootapp.dto.BizCompanyDomainFormDto;
import com.example.springbootapp.service.BizCompanyDomainService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin/company-domains")
public class AdminCompanyDomainApiController {

	private final BizCompanyDomainService bizCompanyDomainService;

	public AdminCompanyDomainApiController(BizCompanyDomainService bizCompanyDomainService) {
		this.bizCompanyDomainService = bizCompanyDomainService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam Long companyId,
			@RequestParam(required = false) String hostName,
			@RequestParam(required = false) String useYn,
			@RequestParam(defaultValue = "200") int limit) {
		try {
			List<Map<String, Object>> domains = bizCompanyDomainService.search(companyId, hostName, useYn, limit);
			Map<String, Object> body = new HashMap<>();
			body.put("domains", domains);
			body.put("count", domains.size());
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@GetMapping("/{domainId}")
	public ResponseEntity<Map<String, Object>> findOne(@PathVariable Long domainId) {
		Map<String, Object> domain = bizCompanyDomainService.findById(domainId);
		if (domain == null) {
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> body = new HashMap<>();
		body.put("domain", domain);
		return ResponseEntity.ok(body);
	}

	@PostMapping(value = "/ssl-cert/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> uploadSslCert(
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) String hostName,
			HttpSession session) {
		try {
			return ResponseEntity.ok(bizCompanyDomainService.uploadSslCertificate(file, hostName, session));
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		} catch (Exception ex) {
			return badRequest("SSL 인증서 업로드에 실패했습니다: " + ex.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> save(@RequestBody BizCompanyDomainFormDto dto, HttpSession session) {
		try {
			Long id = bizCompanyDomainService.save(dto, session);
			Map<String, Object> body = new HashMap<>();
			body.put("success", true);
			body.put("domainId", id);
			body.put("message", dto.getDomainId() == null ? "도메인이 등록되었습니다." : "도메인 정보가 수정되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@DeleteMapping("/{domainId}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long domainId) {
		try {
			bizCompanyDomainService.delete(domainId);
			Map<String, Object> body = new HashMap<>();
			body.put("success", true);
			body.put("message", "도메인이 삭제되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PostMapping("/batch-delete")
	public ResponseEntity<Map<String, Object>> deleteBatch(@RequestBody BizCompanyDomainBulkDeleteDto dto) {
		try {
			int deleted = bizCompanyDomainService.deleteMany(dto != null ? dto.getDomainIds() : null);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("deleted", deleted);
			body.put("message", deleted + "건의 도메인이 삭제되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		body.put("domains", List.of());
		body.put("count", 0);
		return ResponseEntity.badRequest().body(body);
	}
}
