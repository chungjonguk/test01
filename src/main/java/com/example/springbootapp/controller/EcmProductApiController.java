package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.springbootapp.domain.EcmProduct;
import com.example.springbootapp.dto.EcmProductFormDto;
import com.example.springbootapp.service.EcmProductService;
import com.example.springbootapp.service.ProductImageStorageService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/ecommerce/products")
public class EcmProductApiController {

	private final EcmProductService ecmProductService;
	private final ProductImageStorageService productImageStorageService;

	public EcmProductApiController(
			EcmProductService ecmProductService,
			ProductImageStorageService productImageStorageService) {
		this.ecmProductService = ecmProductService;
		this.productImageStorageService = productImageStorageService;
	}

	@PostMapping("/upload-image")
	public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
		try {
			String url = productImageStorageService.store(file);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("url", url);
			body.put("message", "이미지가 업로드되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException | java.io.IOException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam(required = false) String productNm,
			@RequestParam(required = false) String categoryCd,
			@RequestParam(required = false) String statusCd) {
		List<Map<String, Object>> products = ecmProductService.search(productNm, categoryCd, statusCd).stream()
				.map(this::toDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("products", products);
		body.put("count", products.size());
		return ResponseEntity.ok(body);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> get(@PathVariable Long productId) {
		EcmProduct product = ecmProductService.findById(productId);
		if (product == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(toDto(product));
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody EcmProductFormDto dto, HttpSession session) {
		try {
			Long id = ecmProductService.save(dto, session);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("productId", id);
			body.put("message", "상품이 등록되었습니다.");
			return ResponseEntity.status(HttpStatus.CREATED).body(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PutMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> update(
			@PathVariable Long productId,
			@RequestBody EcmProductFormDto dto,
			HttpSession session) {
		dto.setProductId(productId);
		try {
			Long id = ecmProductService.save(dto, session);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("productId", id);
			body.put("message", "상품이 수정되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long productId) {
		try {
			ecmProductService.delete(productId);
			return ResponseEntity.ok(Map.of("success", true, "message", "상품이 삭제되었습니다."));
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

	private Map<String, Object> toDto(EcmProduct p) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("productId", p.getProductId());
		row.put("productNm", p.getProductNm());
		row.put("categoryCd", p.getCategoryCd());
		row.put("price", p.getPrice());
		row.put("stockQty", p.getStockQty());
		row.put("statusCd", p.getStatusCd());
		List<String> imageUrls = ecmProductService.findImageUrls(p.getProductId());
		row.put("imageUrls", imageUrls);
		String mainUrl = p.getImgUrl();
		if (mainUrl == null || mainUrl.isBlank()) {
			mainUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);
		} else {
			mainUrl = EcmProductService.resolveDisplayPath(mainUrl);
		}
		row.put("imgUrl", mainUrl);
		row.put("mainImageUrl", mainUrl);
		row.put("description", p.getDescription());
		row.put("regId", p.getRegId());
		row.put("regDt", p.getRegDt());
		row.put("updateId", p.getUpdateId());
		row.put("updateDt", p.getUpdateDt());
		return row;
	}
}
