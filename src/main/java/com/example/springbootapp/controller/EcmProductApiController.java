package com.example.springbootapp.controller;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.EcmProduct;
import com.example.springbootapp.dto.EcmProductFormDto;
import com.example.springbootapp.dto.ProductExcelImportResult;
import com.example.springbootapp.service.EcmProductService;
import com.example.springbootapp.service.ProductExcelService;
import com.example.springbootapp.service.ProductImageStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
/**
 * 상품 REST API — {@code /api/ecommerce/products}
 * <p>CRUD, NAS 이미지 업로드, 엑셀 일괄 등록·수정·다운로드.</p>
 */
@RestController
@RequestMapping("/api/ecommerce/products")
public class EcmProductApiController {
	private static final MediaType XLSX = MediaType.parseMediaType(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	private final EcmProductService ecmProductService;
	private final ProductImageStorageService productImageStorageService;
	private final ProductExcelService productExcelService;
	private final SessionAuthService sessionAuthService;
	public EcmProductApiController(
			EcmProductService ecmProductService,
			ProductImageStorageService productImageStorageService,
			ProductExcelService productExcelService,
			SessionAuthService sessionAuthService) {
		this.ecmProductService = ecmProductService;
		this.productImageStorageService = productImageStorageService;
		this.productExcelService = productExcelService;
		this.sessionAuthService = sessionAuthService;
	}
	/**
	 * 상품 이미지를 NAS에 저장합니다.
	 *
	 * @param file    in: multipart 이미지 파일
	 * @param session in: 로그인 세션 (등록자 ID 추출)
	 * @return out: {@code { success, fileId, url, filePath, message }}
	 */
	@PostMapping("/upload-image")
	public ResponseEntity<Map<String, Object>> uploadImage(
			@RequestParam("file") MultipartFile file,
			HttpSession session) {
		try {
			String regId = sessionAuthService.getLoginUserId(session);
			if (regId == null || regId.isBlank()) {
				regId = "SYSTEM";
			}
			var stored = productImageStorageService.store(file, regId);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("fileId", stored.fileId());
			body.put("url", stored.url());
			body.put("filePath", stored.filePath());
			body.put("message", "이미지가 업로드되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException | java.io.IOException ex) {
			return badRequest(ex.getMessage());
		}
	}
	/**
	 * 검색 조건에 맞는 상품 목록을 xlsx로 다운로드합니다.
	 *
	 * @param productNm  in: 상품명 (부분 일치, optional)
	 * @param categoryCd in: 카테고리 코드 (optional)
	 * @param statusCd   in: 판매 상태 코드 (optional)
	 * @return out: xlsx 바이트, Content-Disposition 첨부
	 */
	@GetMapping("/excel/export")
	public ResponseEntity<byte[]> exportExcel(
			@RequestParam(required = false) String productNm,
			@RequestParam(required = false) String categoryCd,
			@RequestParam(required = false) String statusCd,
			HttpServletRequest request,
			HttpSession session) {
		try {
			byte[] data = productExcelService.export(productNm, categoryCd, statusCd, request, session);
			return xlsxResponse(data, productExcelService.buildExportFileName());
		} catch (java.io.IOException ex) {
			return ResponseEntity.internalServerError().build();
		}
	}
	/**
	 * 일괄 업로드용 엑셀 양식(헤더 + 샘플 1행)을 다운로드합니다.
	 *
	 * @return out: xlsx 바이트 ({@code product_upload_template.xlsx})
	 */
	@GetMapping("/excel/template")
	public ResponseEntity<byte[]> downloadExcelTemplate() {
		try {
			byte[] data = productExcelService.exportTemplate();
			return xlsxResponse(data, "product_upload_template.xlsx");
		} catch (java.io.IOException ex) {
			return ResponseEntity.internalServerError().build();
		}
	}
	/**
	 * xlsx로 상품을 일괄 등록·수정합니다. 상품ID 없음=등록, DB 존재=수정.
	 *
	 * @param file    in: xlsx 파일
	 * @param session in: 로그인 세션 (등록·수정자 ID)
	 * @return out: {@code { success, created, updated, skipped, errors[], message }}
	 */
	@PostMapping("/excel/import")
	public ResponseEntity<Map<String, Object>> importExcel(
			@RequestParam("file") MultipartFile file,
			HttpServletRequest request,
			HttpSession session) {
		if (file == null || file.isEmpty()) {
			return badRequest("업로드할 엑셀 파일을 선택해 주세요.");
		}
		String name = file.getOriginalFilename();
		if (name == null || (!name.toLowerCase(Locale.ROOT).endsWith(".xlsx")
				&& !name.toLowerCase(Locale.ROOT).endsWith(".xls"))) {
			return badRequest("xlsx 형식의 엑셀 파일만 업로드할 수 있습니다.");
		}
		try {
			ProductExcelImportResult result = productExcelService.importExcel(
					file.getInputStream(), session, request);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", !result.hasErrors() || result.getCreated() > 0 || result.getUpdated() > 0);
			body.put("created", result.getCreated());
			body.put("updated", result.getUpdated());
			body.put("skipped", result.getSkipped());
			body.put("errors", result.getErrors());
			if (result.getCreated() == 0 && result.getUpdated() == 0 && result.hasErrors()) {
				body.put("message", "엑셀 업로드에 실패했습니다.");
				return ResponseEntity.badRequest().body(body);
			}
			body.put("message", "엑셀 업로드가 완료되었습니다. (등록 " + result.getCreated()
					+ "건, 수정 " + result.getUpdated() + "건, 건너뜀 " + result.getSkipped() + "건)");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		} catch (java.io.IOException ex) {
			return badRequest("엑셀 파일을 읽는 중 오류가 발생했습니다.");
		}
	}
	/**
	 * 상품 목록을 조회합니다.
	 *
	 * @param productNm  in: 상품명 (optional)
	 * @param categoryCd in: 카테고리 코드 (optional)
	 * @param statusCd   in: 판매 상태 (optional)
	 * @return out: {@code { products: [...], count }}
	 */
	/**
	 * 고객 스토어(업체 Host) — ACTIVE 상품만, 테넌트 업체 스코프.
	 */
	@GetMapping("/store-catalog")
	public ResponseEntity<Map<String, Object>> storeCatalog(HttpServletRequest request) {
		try {
			List<Map<String, Object>> products = ecmProductService.searchStoreCatalog(request).stream()
					.map(this::toDto)
					.collect(Collectors.toList());
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("products", products);
			body.put("count", products.size());
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam(required = false) String productNm,
			@RequestParam(required = false) String categoryCd,
			@RequestParam(required = false) String statusCd,
			HttpServletRequest request,
			HttpSession session) {
		List<Map<String, Object>> products = ecmProductService
				.search(productNm, categoryCd, statusCd, request, session)
				.stream()
				.map(this::toDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("products", products);
		body.put("count", products.size());
		return ResponseEntity.ok(body);
	}
	/**
	 * 상품 단건을 조회합니다.
	 *
	 * @param productId in: 상품 ID
	 * @return out: 상품 JSON (이미지 URL 포함), 없으면 404
	 */
	@GetMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> get(
			@PathVariable Long productId,
			HttpServletRequest request,
			HttpSession session) {
		EcmProduct product = ecmProductService.findByIdForScope(productId, request, session);
		if (product == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(toDto(product));
	}
	/**
	 * 상품을 등록합니다.
	 *
	 * @param dto     in: 상품 입력 JSON
	 * @param session in: 로그인 세션
	 * @return out: 201 {@code { success, productId, message }}
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> create(
			@RequestBody EcmProductFormDto dto,
			HttpServletRequest request,
			HttpSession session) {
		try {
			Long id = ecmProductService.save(dto, session, request);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("productId", id);
			body.put("message", "상품이 등록되었습니다.");
			return ResponseEntity.status(HttpStatus.CREATED).body(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}
	/**
	 * 상품을 수정합니다.
	 *
	 * @param productId in: 상품 ID (path)
	 * @param dto       in: 수정할 필드 JSON
	 * @param session   in: 로그인 세션
	 * @return out: {@code { success, productId, message }}
	 */
	@PutMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> update(
			@PathVariable Long productId,
			@RequestBody EcmProductFormDto dto,
			HttpServletRequest request,
			HttpSession session) {
		dto.setProductId(productId);
		try {
			Long id = ecmProductService.save(dto, session, request);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("productId", id);
			body.put("message", "상품이 수정되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}
	/**
	 * 상품을 삭제합니다 (연관 이미지 포함).
	 *
	 * @param productId in: 상품 ID
	 * @return out: {@code { success, message }}
	 */
	@DeleteMapping("/{productId}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long productId) {
		try {
			ecmProductService.delete(productId);
			return ResponseEntity.ok(Map.of("success", true, "message", "상품이 삭제되었습니다."));
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}
	private ResponseEntity<byte[]> xlsxResponse(byte[] data, String fileName) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(fileName))
				.contentType(XLSX)
				.body(data);
	}
	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
	private static String contentDisposition(String fileName) {
		String asciiName = fileName.replaceAll("[^\\x20-\\x7E]", "_");
		return "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''"
				+ java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8)
						.replace("+", "%20");
	}
	private Map<String, Object> toDto(EcmProduct p) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("productId", p.getProductId());
		row.put("companyId", p.getCompanyId());
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
