package com.example.springbootapp.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 로그인 세션에 저장되는 메뉴·권한 스냅샷.
 */
public class MenuAccessSnapshot implements Serializable {
	private static final long serialVersionUID = 1L;

	private String roleCd;
	private boolean writeAll;
	private List<Long> allowedCompanyIds = new ArrayList<>();
	private List<String> allowedMenuPaths = new ArrayList<>();

	public String getRoleCd() {
		return roleCd;
	}

	public void setRoleCd(String roleCd) {
		this.roleCd = roleCd;
	}

	public boolean isWriteAll() {
		return writeAll;
	}

	public void setWriteAll(boolean writeAll) {
		this.writeAll = writeAll;
	}

	public List<Long> getAllowedCompanyIds() {
		return allowedCompanyIds;
	}

	public void setAllowedCompanyIds(List<Long> allowedCompanyIds) {
		this.allowedCompanyIds = allowedCompanyIds != null ? allowedCompanyIds : new ArrayList<>();
	}

	public List<String> getAllowedMenuPaths() {
		return allowedMenuPaths;
	}

	public void setAllowedMenuPaths(List<String> allowedMenuPaths) {
		this.allowedMenuPaths = allowedMenuPaths != null ? allowedMenuPaths : new ArrayList<>();
	}

	public AppRole role() {
		return AppRole.fromCode(roleCd);
	}

	public boolean isMenuAllowed(String menuPath) {
		if (menuPath == null || menuPath.isBlank()) {
			return false;
		}
		if (writeAll || role() == AppRole.PLATFORM_ADMIN) {
			return true;
		}
		String key = normalizeMenuPath(menuPath);
		for (String allowed : allowedMenuPaths) {
			if (key.equals(normalizeMenuPath(allowed))) {
				return true;
			}
		}
		return false;
	}

	public static String normalizeMenuPath(String path) {
		if (path == null) {
			return "";
		}
		String p = path.trim();
		int q = p.indexOf('?');
		if (q >= 0) {
			p = p.substring(0, q);
		}
		if (p.length() > 3 && p.endsWith(".do")) {
			p = p.substring(0, p.length() - 3);
		}
		if (p.length() > 1 && p.endsWith("/")) {
			p = p.substring(0, p.length() - 1);
		}
		if ("/index".equals(p)) {
			return "/";
		}
		return p;
	}

	public List<String> allowedMenuPathsView() {
		return Collections.unmodifiableList(allowedMenuPaths);
	}
}
