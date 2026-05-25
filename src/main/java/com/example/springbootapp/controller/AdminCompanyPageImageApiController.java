package com.example.springbootapp.controller;

import com.example.springbootapp.config.CompanyPageImageCatalog;
import com.example.springbootapp.service.BizCompanyPageImageService;
import com.example.springbootapp.service.DashboardCompanySessionService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/company-page-images")
public class AdminCompanyPageImageApiController {

	private final BizCompanyPageImageService pageImageService;
	private final DashboardCompanySessionService companySessionService;

	public AdminCompanyPageImageApiController(
			BizCompanyPageImageService pageImageService,
			DashboardCompanySessionService companySessionService) {
		this.pageImageService = pageImageService;
		this.companySessionService = companySessionService;
	}

	@GetMapping("/slots")
	public List<Map<String, Object>> slots() {
		return CompanyPageImageCatalog.toMaps();
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam Long companyId,
			HttpSession session) {
		try {
			companySessionService.setSelectedCompanyId(session, companyId);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("companyId", companyId);
			body.put("slots", pageImageService.listWithSlots(companyId));
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> upload(
			@RequestParam Long companyId,
			@RequestParam String pageCd,
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) String altText,
			HttpSession session) {
		try {
			companySessionService.setSelectedCompanyId(session, companyId);
			Map<String, Object> image = pageImageService.upload(companyId, pageCd, altText, file, session);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("image", image);
			body.put("message", "이미지가 등록되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		} catch (IOException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@DeleteMapping("/{imageId}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long imageId) {
		try {
			pageImageService.delete(imageId);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("message", "이미지 등록이 삭제되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
}
