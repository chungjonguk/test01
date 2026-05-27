package com.example.springbootapp.service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.EcmProduct;
import com.example.springbootapp.domain.EcmProductImage;
import com.example.springbootapp.dto.EcmProductFormDto;
import com.example.springbootapp.dto.InventoryStockAdjustDto;
import com.example.springbootapp.mapper.EcmProductImageMapper;
import com.example.springbootapp.mapper.EcmProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
/**
 * 이커머스 상품 및 상품 이미지 조회·등록·수정·삭제를 처리하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class EcmProductService {

	/** 재고 부족(LOW) 판정 상한 — 이하이면서 1 이상 */
	public static final int INVENTORY_LOW_THRESHOLD = 10;

	private final EcmProductMapper ecmProductMapper;
	private final EcmProductImageMapper ecmProductImageMapper;
	private final SessionAuthService sessionAuthService;
	private final CompanyTenantContext companyTenantContext;
	private final DashboardCompanySessionService companySessionService;

	public EcmProductService(
			EcmProductMapper ecmProductMapper,
			EcmProductImageMapper ecmProductImageMapper,
			SessionAuthService sessionAuthService,
			CompanyTenantContext companyTenantContext,
			DashboardCompanySessionService companySessionService) {
		this.ecmProductMapper = ecmProductMapper;
		this.ecmProductImageMapper = ecmProductImageMapper;
		this.sessionAuthService = sessionAuthService;
		this.companyTenantContext = companyTenantContext;
		this.companySessionService = companySessionService;
	}
	/**
	 * 조건에 맞는 상품 목록을 검색한다.
	 *
	 * @param productNm  상품명 (부분 일치, null 허용)
	 * @param categoryCd 카테고리 코드 (null 허용)
	 * @param statusCd   판매 상태 코드 (null 허용)
	 * @return 상품 엔티티 목록
	 */
	public List<EcmProduct> search(String productNm, String categoryCd, String statusCd) {
		return search(productNm, categoryCd, statusCd, null, null);
	}

	public List<EcmProduct> search(
			String productNm, String categoryCd, String statusCd, HttpServletRequest request, HttpSession session) {
		Long companyId = companyTenantContext.resolveProductScopeCompanyId(request, session);
		return ecmProductMapper.findAll(
				companyId, trimToNull(productNm), trimToNull(categoryCd), trimToNull(statusCd));
	}

	/** 고객 스토어: Host 테넌트 업체의 판매중(ACTIVE) 상품만 */
	public List<EcmProduct> searchStoreCatalog(HttpServletRequest request) {
		Long companyId = companyTenantContext.resolveTenantCompanyId(request);
		if (companyId == null) {
			throw new IllegalArgumentException("업체 도메인으로 접속해 주세요.");
		}
		return ecmProductMapper.findAll(
				companyId, null, null, CompanyTenantContext.STORE_CATALOG_STATUS);
	}

	/**
	 * 재고 관리 화면용 상품 목록.
	 *
	 * @param stockFilter ALL|ZERO|LOW|OK (null/blank → ALL)
	 */
	public List<EcmProduct> searchInventory(
			String productNm, String categoryCd, String statusCd, String stockFilter) {
		String filter = trimToNull(stockFilter);
		if (filter == null) {
			filter = "ALL";
		}
		return searchInventory(productNm, categoryCd, statusCd, stockFilter, null, null);
	}

	public List<EcmProduct> searchInventory(
			String productNm,
			String categoryCd,
			String statusCd,
			String stockFilter,
			HttpServletRequest request,
			HttpSession session) {
		String filter = trimToNull(stockFilter);
		if (filter == null) {
			filter = "ALL";
		}
		Long companyId = companyTenantContext.resolveProductScopeCompanyId(request, session);
		return ecmProductMapper.findForInventory(
				companyId,
				trimToNull(productNm),
				trimToNull(categoryCd),
				trimToNull(statusCd),
				filter,
				INVENTORY_LOW_THRESHOLD);
	}

	/**
	 * 재고 수량을 조정합니다.
	 */
	@Transactional
	public EcmProduct adjustStock(Long productId, InventoryStockAdjustDto dto, HttpSession session) {
		if (productId == null) {
			throw new IllegalArgumentException("상품 ID가 필요합니다.");
		}
		if (dto == null || isBlank(dto.getAdjustType())) {
			throw new IllegalArgumentException("조정 유형을 선택해 주세요.");
		}
		if (dto.getQuantity() == null || dto.getQuantity() < 0) {
			throw new IllegalArgumentException("수량을 올바르게 입력해 주세요.");
		}
		EcmProduct existing = ecmProductMapper.findById(productId);
		if (existing == null) {
			throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
		}
		int current = existing.getStockQty() != null ? existing.getStockQty() : 0;
		int next;
		String type = dto.getAdjustType().trim().toUpperCase();
		switch (type) {
			case "SET" -> next = dto.getQuantity();
			case "ADD" -> next = current + dto.getQuantity();
			case "SUB" -> next = current - dto.getQuantity();
			default -> throw new IllegalArgumentException("조정 유형은 SET, ADD, SUB 중 하나여야 합니다.");
		}
		if (next < 0) {
			throw new IllegalArgumentException("재고는 0 미만이 될 수 없습니다.");
		}
		String actor = resolveActor(session);
		int updated = ecmProductMapper.updateStockQty(productId, next, actor);
		if (updated == 0) {
			throw new IllegalArgumentException("재고 반영에 실패했습니다.");
		}
		return ecmProductMapper.findById(productId);
	}
	/**
	 * 상품 ID로 단건을 조회한다.
	 *
	 * @param productId 상품 ID
	 * @return 상품 엔티티, ID가 null이거나 없으면 null
	 */
	public EcmProduct findById(Long productId) {
		if (productId == null) {
			return null;
		}
		return ecmProductMapper.findById(productId);
	}

	public EcmProduct findByIdForScope(Long productId, HttpServletRequest request, HttpSession session) {
		EcmProduct product = findById(productId);
		if (product == null) {
			return null;
		}
		Long scopeCompanyId = companyTenantContext.resolveProductScopeCompanyId(request, session);
		if (scopeCompanyId != null && product.getCompanyId() != null
				&& !scopeCompanyId.equals(product.getCompanyId())) {
			return null;
		}
		if (companyTenantContext.resolveTenantCompanyId(request) != null
				&& !CompanyTenantContext.STORE_CATALOG_STATUS.equalsIgnoreCase(product.getStatusCd())) {
			return null;
		}
		return product;
	}
	/**
	 * 상품에 연결된 이미지 목록을 조회한다. 별도 이미지가 없으면 레거시 imgUrl을 반환한다.
	 *
	 * @param productId 상품 ID
	 * @return 상품 이미지 목록
	 */
	public List<EcmProductImage> findImages(Long productId) {
		if (productId == null) {
			return List.of();
		}
		List<EcmProductImage> images = ecmProductImageMapper.findByProductId(productId);
		if (!images.isEmpty()) {
			return images;
		}
		EcmProduct product = ecmProductMapper.findById(productId);
		if (product != null && product.getImgUrl() != null && !product.getImgUrl().isBlank()) {
			EcmProductImage legacy = new EcmProductImage();
			legacy.setProductId(productId);
			legacy.setSortOrd(1);
			legacy.setImgUrl(product.getImgUrl().trim());
			return List.of(legacy);
		}
		return List.of();
	}
	/**
	 * 상품 이미지 URL 목록을 조회한다. 대표 이미지가 맨 앞에 오도록 정렬한다.
	 *
	 * @param productId 상품 ID
	 * @return 표시용 이미지 URL 목록
	 */
	public List<String> findImageUrls(Long productId) {
		List<String> urls = findImages(productId).stream()
				.map(EcmProductImage::getImgUrl)
				.map(EcmProductService::resolveDisplayPath)
				.toList();
		EcmProduct product = ecmProductMapper.findById(productId);
		String main = product != null ? trimToNull(product.getImgUrl()) : null;
		return orderUrlsWithMain(urls, main);
	}
	/**
	 * 상품을 신규 등록하거나 기존 상품을 수정한다.
	 *
	 * @param dto     상품 입력 폼
	 * @param session HTTP 세션 (등록·수정자 ID 추출용)
	 * @return 저장된 상품 ID
	 */
	@Transactional
	public Long save(EcmProductFormDto dto, HttpSession session) {
		return save(dto, session, null);
	}

	@Transactional
	public Long save(EcmProductFormDto dto, HttpSession session, HttpServletRequest request) {
		validate(dto);
		List<String> imageUrls = normalizeImageUrls(dto);
		String actor = resolveActor(session);
		EcmProduct product = toEntity(dto);
		Long scopeCompanyId = resolveSaveCompanyId(dto, request, session);
		product.setCompanyId(scopeCompanyId);
		product.setImgUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
		product.setRegId(actor);
		product.setUpdateId(actor);
		if (dto.getProductId() == null) {
			ecmProductMapper.insert(product);
			saveImages(product.getProductId(), imageUrls, actor);
			return product.getProductId();
		}
		EcmProduct existing = ecmProductMapper.findById(dto.getProductId());
		if (existing == null) {
			throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
		}
		if (scopeCompanyId != null && existing.getCompanyId() != null
				&& !scopeCompanyId.equals(existing.getCompanyId())) {
			throw new IllegalArgumentException("다른 업체의 상품은 수정할 수 없습니다.");
		}
		product.setProductId(dto.getProductId());
		if (existing.getCompanyId() != null) {
			product.setCompanyId(existing.getCompanyId());
		}
		ecmProductMapper.update(product);
		saveImages(product.getProductId(), imageUrls, actor);
		return product.getProductId();
	}
	/**
	 * 상품과 연결된 이미지를 삭제한다.
	 *
	 * @param productId 삭제할 상품 ID
	 */
	@Transactional
	public void delete(Long productId) {
		if (productId == null) {
			throw new IllegalArgumentException("상품 ID가 필요합니다.");
		}
		if (ecmProductMapper.findById(productId) == null) {
			throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
		}
		ecmProductImageMapper.deleteByProductId(productId);
		ecmProductMapper.deleteById(productId);
	}
	private void saveImages(Long productId, List<String> imageUrls, String actor) {
		ecmProductImageMapper.deleteByProductId(productId);
		int ord = 1;
		for (String url : imageUrls) {
			EcmProductImage image = new EcmProductImage();
			image.setProductId(productId);
			image.setSortOrd(ord++);
			image.setImgUrl(url);
			image.setRegId(actor);
			ecmProductImageMapper.insert(image);
		}
	}
	private List<String> normalizeImageUrls(EcmProductFormDto dto) {
		Set<String> unique = new LinkedHashSet<>();
		if (dto.getImageUrls() != null) {
			for (String url : dto.getImageUrls()) {
				String t = trimToNull(url);
				if (t != null) {
					unique.add(resolveDisplayPath(t));
				}
			}
		}
		if (unique.isEmpty()) {
			String legacy = trimToNull(dto.getImgUrl());
			if (legacy != null) {
				unique.add(resolveDisplayPath(legacy));
			}
		}
		if (unique.size() > EcmProductFormDto.MAX_IMAGES) {
			throw new IllegalArgumentException("상품 이미지는 최대 " + EcmProductFormDto.MAX_IMAGES + "개까지 등록할 수 있습니다.");
		}
		List<String> urls = new ArrayList<>(unique);
		String main = trimToNull(dto.getMainImageUrl());
		if (main == null) {
			main = trimToNull(dto.getImgUrl());
		}
		if (main != null) {
			String mainResolved = resolveDisplayPath(main);
			boolean found = urls.stream().anyMatch(u -> resolveDisplayPath(u).equals(mainResolved));
			if (!found) {
				throw new IllegalArgumentException("대표 이미지는 등록한 이미지 URL 중에서 선택해 주세요.");
			}
		}
		return orderUrlsWithMain(urls, main);
	}
	/**
	 * 대표 이미지 URL을 목록 맨 앞으로 배치한다 (ecm_product.img_url · sort_ord 1과 동기화).
	 *
	 * @param urls    이미지 URL 목록
	 * @param mainUrl 대표 이미지 URL
	 * @return 대표 이미지가 선두인 URL 목록
	 */
	public static List<String> orderUrlsWithMain(List<String> urls, String mainUrl) {
		if (urls == null || urls.isEmpty() || mainUrl == null || mainUrl.isBlank()) {
			return urls == null ? List.of() : new ArrayList<>(urls);
		}
		String mainResolved = resolveDisplayPath(mainUrl);
		String matched = null;
		List<String> rest = new ArrayList<>();
		for (String url : urls) {
			if (matched == null && resolveDisplayPath(url).equals(mainResolved)) {
				matched = url;
			} else {
				rest.add(url);
			}
		}
		if (matched == null) {
			return new ArrayList<>(urls);
		}
		List<String> ordered = new ArrayList<>();
		ordered.add(matched);
		ordered.addAll(rest);
		return ordered;
	}
	/**
	 * 이미지 URL을 화면 표시용 경로로 정규화한다.
	 *
	 * @param imgUrl 원본 이미지 URL 또는 경로
	 * @return 절대 URL 또는 슬래시로 시작하는 경로 (빈 값이면 기본 이미지)
	 */
	public static String resolveDisplayPath(String imgUrl) {
		if (imgUrl == null || imgUrl.isBlank()) {
			return "/assets/img/products/1.jpg";
		}
		String trimmed = imgUrl.trim();
		if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("/")) {
			return trimmed;
		}
		return "/" + trimmed;
	}
	private void validate(EcmProductFormDto dto) {
		if (dto == null) {
			throw new IllegalArgumentException("입력값이 없습니다.");
		}
		if (isBlank(dto.getProductNm())) {
			throw new IllegalArgumentException("상품명을 입력해 주세요.");
		}
		if (isBlank(dto.getCategoryCd())) {
			throw new IllegalArgumentException("카테고리를 선택해 주세요.");
		}
		if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("가격을 올바르게 입력해 주세요.");
		}
		if (dto.getStockQty() == null || dto.getStockQty() < 0) {
			throw new IllegalArgumentException("재고 수량을 올바르게 입력해 주세요.");
		}
		if (isBlank(dto.getStatusCd())) {
			throw new IllegalArgumentException("판매 상태를 선택해 주세요.");
		}
	}
	private EcmProduct toEntity(EcmProductFormDto dto) {
		EcmProduct p = new EcmProduct();
		p.setProductNm(dto.getProductNm().trim());
		p.setCategoryCd(dto.getCategoryCd().trim());
		p.setPrice(dto.getPrice());
		p.setStockQty(dto.getStockQty());
		p.setStatusCd(dto.getStatusCd().trim());
		p.setDescription(trimToNull(dto.getDescription()));
		return p;
	}
	private Long resolveSaveCompanyId(EcmProductFormDto dto, HttpServletRequest request, HttpSession session) {
		Long scope = companyTenantContext.resolveProductScopeCompanyId(request, session);
		if (dto.getCompanyId() != null) {
			if (scope != null && !scope.equals(dto.getCompanyId())) {
				throw new IllegalArgumentException("선택 업체와 일치하지 않는 companyId입니다.");
			}
			return dto.getCompanyId();
		}
		if (scope != null) {
			return scope;
		}
		return companySessionService.resolveSelectedCompanyId(session);
	}

	private String resolveActor(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		return userId != null && !userId.isBlank() ? userId : "SYSTEM";
	}
	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		return t.isEmpty() ? null : t;
	}
	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
