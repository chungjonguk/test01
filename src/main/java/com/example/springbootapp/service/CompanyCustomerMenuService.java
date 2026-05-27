package com.example.springbootapp.service;

import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.MenuRoleCatalog;
import com.example.springbootapp.domain.CompanyCustomerMenu;
import com.example.springbootapp.mapper.BizCompanyMapper;
import com.example.springbootapp.mapper.CompanyCustomerMenuMapper;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CompanyCustomerMenuService {

	private final CompanyCustomerMenuMapper companyCustomerMenuMapper;
	private final BizCompanyMapper bizCompanyMapper;
	private final SessionAuthService sessionAuthService;

	public CompanyCustomerMenuService(
			CompanyCustomerMenuMapper companyCustomerMenuMapper,
			BizCompanyMapper bizCompanyMapper,
			SessionAuthService sessionAuthService) {
		this.companyCustomerMenuMapper = companyCustomerMenuMapper;
		this.bizCompanyMapper = bizCompanyMapper;
		this.sessionAuthService = sessionAuthService;
	}

	public Map<String, Object> listForCompany(Long companyId) {
		requireCompany(companyId);
		List<Map<String, Object>> enabled = companyCustomerMenuMapper.listByCompanyId(companyId).stream()
				.map(this::toMap)
				.toList();
		List<Map<String, Object>> candidates = new ArrayList<>();
		int ord = 0;
		for (String path : MenuRoleCatalog.CUSTOMER_MENU_CANDIDATES) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("menuPath", path);
			item.put("label", menuLabel(path));
			item.put("sortOrd", ord++);
			item.put("enabled", enabled.stream().anyMatch(m -> path.equals(m.get("menuPath"))));
			candidates.add(item);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("companyId", companyId);
		body.put("enabledMenus", enabled);
		body.put("candidates", candidates);
		return body;
	}

	@Transactional
	public void saveMenus(Long companyId, List<String> menuPaths, HttpSession session) {
		requireCompany(companyId);
		String actor = resolveActor(session);
		companyCustomerMenuMapper.deleteByCompanyId(companyId);
		if (menuPaths == null || menuPaths.isEmpty()) {
			return;
		}
		int ord = 0;
		for (String raw : menuPaths) {
			if (raw == null || raw.isBlank()) {
				continue;
			}
			String path = raw.trim();
			if (!MenuRoleCatalog.CUSTOMER_MENU_CANDIDATES.contains(path)) {
				throw new IllegalArgumentException("허용되지 않은 메뉴 경로: " + path);
			}
			CompanyCustomerMenu row = new CompanyCustomerMenu();
			row.setCompanyId(companyId);
			row.setMenuPath(path);
			row.setUseYn("Y");
			row.setSortOrd(ord++);
			row.setRegId(actor);
			row.setUpdateId(actor);
			companyCustomerMenuMapper.insert(row);
		}
	}

	private Map<String, Object> toMap(CompanyCustomerMenu row) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("menuPath", row.getMenuPath());
		map.put("label", menuLabel(row.getMenuPath()));
		map.put("sortOrd", row.getSortOrd());
		return map;
	}

	private static String menuLabel(String path) {
		return switch (path) {
			case "/shop-home" -> "쇼핑몰 홈";
			case "/app/e-commerce/product/product-grid" -> "상품 목록";
			case "/app/e-commerce/product/product-details" -> "상품 상세";
			case "/app/e-commerce/shopping-cart" -> "장바구니";
			case "/app/e-commerce/checkout" -> "결제";
			case "/pages/faq/faq-basic" -> "FAQ";
			case "/pages/user/profile", "/pages/user/settings" -> "프로필 설정";
			default -> path;
		};
	}

	private void requireCompany(Long companyId) {
		if (companyId == null) {
			throw new IllegalArgumentException("업체를 선택하세요.");
		}
		if (bizCompanyMapper.findById(companyId) == null) {
			throw new IllegalArgumentException("업체를 찾을 수 없습니다.");
		}
	}

	private String resolveActor(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		return userId != null && !userId.isBlank() ? userId : "SYSTEM";
	}
}
