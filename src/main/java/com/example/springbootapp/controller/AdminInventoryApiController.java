package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.domain.EcmProduct;
import com.example.springbootapp.dto.InventoryStockAdjustDto;
import com.example.springbootapp.service.EcmProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 재고 관리 REST API — {@code /api/admin/inventory}
 */
@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryApiController {

	private final EcmProductService ecmProductService;

	public AdminInventoryApiController(EcmProductService ecmProductService) {
		this.ecmProductService = ecmProductService;
	}

	/**
	 * 재고 목록 조회.
	 *
	 * @param stockFilter ALL|ZERO|LOW|OK (optional)
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam(required = false) String productNm,
			@RequestParam(required = false) String categoryCd,
			@RequestParam(required = false) String statusCd,
			@RequestParam(required = false) String stockFilter,
			HttpServletRequest request,
			HttpSession session) {
		List<Map<String, Object>> items = ecmProductService
				.searchInventory(productNm, categoryCd, statusCd, stockFilter, request, session)
				.stream()
				.map(this::toRow)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("items", items);
		body.put("count", items.size());
		return ResponseEntity.ok(body);
	}

	/**
	 * 단건 재고 조정.
	 */
	@PostMapping("/{productId}/adjust")
	public ResponseEntity<Map<String, Object>> adjust(
			@PathVariable Long productId,
			@RequestBody InventoryStockAdjustDto dto,
			HttpSession session) {
		try {
			EcmProduct updated = ecmProductService.adjustStock(productId, dto, session);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("item", toRow(updated));
			body.put("message", "재고가 반영되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", false);
			body.put("message", ex.getMessage());
			return ResponseEntity.badRequest().body(body);
		}
	}

	private Map<String, Object> toRow(EcmProduct p) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("productId", p.getProductId());
		row.put("productNm", p.getProductNm());
		row.put("categoryCd", p.getCategoryCd());
		row.put("price", p.getPrice());
		row.put("stockQty", p.getStockQty());
		row.put("statusCd", p.getStatusCd());
		row.put("updateDt", p.getUpdateDt());
		row.put("updateId", p.getUpdateId());
		int qty = p.getStockQty() != null ? p.getStockQty() : 0;
		String stockLevel;
		if (qty <= 0) {
			stockLevel = "ZERO";
		} else if (qty <= EcmProductService.INVENTORY_LOW_THRESHOLD) {
			stockLevel = "LOW";
		} else {
			stockLevel = "OK";
		}
		row.put("stockLevel", stockLevel);
		return row;
	}
}
