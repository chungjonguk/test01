package com.example.springbootapp.service;

import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
import com.example.springbootapp.config.MenuRoleCatalog;
import com.example.springbootapp.config.web.DoPathHelper;
import com.example.springbootapp.domain.CompanyCustomerMenu;
import com.example.springbootapp.domain.UserAuthProfile;
import com.example.springbootapp.mapper.CompanyCustomerMenuMapper;
import com.example.springbootapp.mapper.UserAuthProfileMapper;
import com.example.springbootapp.mapper.UserCompanyMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuAccessService {

	private final UserAuthProfileMapper userAuthProfileMapper;
	private final UserCompanyMapper userCompanyMapper;
	private final CompanyCustomerMenuMapper companyCustomerMenuMapper;
	private final ScreenListService screenListService;

	public MenuAccessService(
			UserAuthProfileMapper userAuthProfileMapper,
			UserCompanyMapper userCompanyMapper,
			CompanyCustomerMenuMapper companyCustomerMenuMapper,
			ScreenListService screenListService) {
		this.userAuthProfileMapper = userAuthProfileMapper;
		this.userCompanyMapper = userCompanyMapper;
		this.companyCustomerMenuMapper = companyCustomerMenuMapper;
		this.screenListService = screenListService;
	}

	public MenuAccessSnapshot resolveForUser(String userId) {
		MenuAccessSnapshot snap = new MenuAccessSnapshot();
		if (userId == null || userId.isBlank()) {
			snap.setRoleCd(AppRole.COMPANY_CUSTOMER.name());
			snap.setAllowedMenuPaths(List.of());
			return snap;
		}
		UserAuthProfile profile = userAuthProfileMapper.findByUserId(userId);
		if (profile == null) {
			// 권한 미등록(기존 계정): 플랫폼 관리자와 동일 — 마이그레이션 호환
			snap.setRoleCd(AppRole.PLATFORM_ADMIN.name());
			snap.setWriteAll(true);
			snap.setAllowedMenuPaths(allActiveMenuPaths());
			return snap;
		}
		AppRole role = AppRole.fromCode(profile.getRoleCd());
		if (role == null) {
			role = AppRole.COMPANY_CUSTOMER;
		}
		snap.setRoleCd(role.name());
		List<Long> companyIds = userCompanyMapper.findCompanyIdsByUserId(userId);
		snap.setAllowedCompanyIds(companyIds);

		if (role == AppRole.PLATFORM_ADMIN) {
			snap.setWriteAll(true);
			snap.setAllowedMenuPaths(allActiveMenuPaths());
			return snap;
		}
		snap.setWriteAll(false);
		if (role == AppRole.COMPANY_ADMIN) {
			snap.setAllowedMenuPaths(new ArrayList<>(MenuRoleCatalog.COMPANY_ADMIN_PATHS));
			return snap;
		}
		snap.setAllowedMenuPaths(resolveCustomerMenuPaths(companyIds));
		return snap;
	}

	public boolean canAccessUri(LoginSession session, String requestUri) {
		if (session == null || session.getMenuAccess() == null) {
			return true;
		}
		MenuAccessSnapshot access = session.getMenuAccess();
		if (access.isWriteAll()) {
			return true;
		}
		String path = MenuAccessSnapshot.normalizeMenuPath(DoPathHelper.stripDoSuffix(requestUri));
		if (isPublicPath(path)) {
			return true;
		}
		if (path.startsWith("/api/")) {
			return canAccessApi(access, path);
		}
		AppRole role = access.role();
		if (role == AppRole.COMPANY_ADMIN || role == AppRole.COMPANY_CUSTOMER) {
			for (String platformOnly : MenuRoleCatalog.PLATFORM_ONLY_PATHS) {
				if (path.equals(platformOnly) || path.startsWith(platformOnly + "/")) {
					return false;
				}
			}
		}
		return access.isMenuAllowed(path);
	}

	private boolean canAccessApi(MenuAccessSnapshot access, String path) {
		if (isKakaoLocalApi(path)) {
			return true;
		}
		if (isUserSelfServiceApi(path)) {
			return true;
		}
		AppRole role = access.role();
		if (role == AppRole.PLATFORM_ADMIN) {
			return true;
		}
		if (role == AppRole.COMPANY_ADMIN) {
			if (path.startsWith("/api/admin/codes")
					|| path.startsWith("/api/admin/menus")
					|| path.startsWith("/api/admin/user-auth")
					|| path.startsWith("/api/admin/table-sequences")
					|| path.startsWith("/api/admin/user-access-logs")) {
				return false;
			}
			return path.startsWith("/api/ecommerce/")
					|| path.startsWith("/api/admin/companies")
					|| path.startsWith("/api/admin/company-domains")
					|| path.startsWith("/api/admin/company-page-images")
					|| path.startsWith("/api/admin/company-customer-menus")
					|| path.startsWith("/api/dashboard/")
					|| path.startsWith("/api/admin/inventory")
					|| path.startsWith("/api/url/");
		}
		if (role == AppRole.COMPANY_CUSTOMER) {
			return path.startsWith("/api/ecommerce/products/store-catalog")
					|| path.matches("/api/ecommerce/products/\\d+")
					|| path.startsWith("/api/url/");
		}
		return false;
	}

	public boolean canWriteApi(LoginSession session, String method, String requestUri) {
		if (session == null || session.getMenuAccess() == null) {
			return true;
		}
		if (session.getMenuAccess().isWriteAll()) {
			return true;
		}
		String upper = method != null ? method.toUpperCase() : "GET";
		if ("GET".equals(upper) || "HEAD".equals(upper) || "OPTIONS".equals(upper)) {
			return canAccessUri(session, requestUri);
		}
		AppRole role = session.getMenuAccess().role();
		if (role == AppRole.COMPANY_CUSTOMER) {
			String path = requestUri != null ? requestUri.split("\\?")[0] : "";
			return isUserSelfServiceApi(path);
		}
		if (role == AppRole.COMPANY_ADMIN) {
			return isCompanyAdminWritableApi(requestUri);
		}
		return false;
	}

	private static boolean isKakaoLocalApi(String path) {
		return path != null && path.startsWith("/api/kakao/local");
	}

	private static boolean isUserSelfServiceApi(String path) {
		return path != null
				&& (path.startsWith("/api/user/profile-images")
						|| path.startsWith("/api/user/profile-settings"));
	}

	private boolean isCompanyAdminWritableApi(String uri) {
		if (uri == null) {
			return false;
		}
		String path = uri.split("\\?")[0];
		if (isUserSelfServiceApi(path)) {
			return true;
		}
		return path.startsWith("/api/ecommerce/")
				|| path.startsWith("/api/admin/companies")
				|| path.startsWith("/api/admin/company-domains")
				|| path.startsWith("/api/admin/company-page-images")
				|| path.startsWith("/api/admin/company-customer-menus")
				|| path.startsWith("/api/dashboard/")
				|| path.startsWith("/api/admin/inventory");
	}

	private List<String> resolveCustomerMenuPaths(List<Long> companyIds) {
		Set<String> paths = new LinkedHashSet<>();
		if (companyIds == null || companyIds.isEmpty()) {
			paths.addAll(MenuRoleCatalog.DEFAULT_CUSTOMER_MENU_PATHS);
			return new ArrayList<>(paths);
		}
		for (Long companyId : companyIds) {
			List<CompanyCustomerMenu> rows = companyCustomerMenuMapper.listByCompanyId(companyId);
			if (rows.isEmpty()) {
				paths.addAll(MenuRoleCatalog.DEFAULT_CUSTOMER_MENU_PATHS);
			} else {
				for (CompanyCustomerMenu row : rows) {
					paths.add(row.getMenuPath());
				}
			}
		}
		return new ArrayList<>(paths);
	}

	private List<String> allActiveMenuPaths() {
		List<String> paths = new ArrayList<>();
		for (String uri : screenListService.findActiveUriPaths()) {
			paths.add(MenuAccessSnapshot.normalizeMenuPath(uri));
		}
		paths.addAll(MenuRoleCatalog.COMPANY_ADMIN_PATHS);
		return paths.stream().distinct().toList();
	}

	private boolean isPublicPath(String path) {
		return path.startsWith("/auth/")
				|| path.startsWith("/pages/authentication/")
				|| path.startsWith("/pages/errors/")
				|| "/".equals(path)
				|| "/dashboard".equals(path)
				|| "/dashboard-home".equals(path);
	}
}
