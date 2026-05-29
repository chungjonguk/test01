package com.example.springbootapp.config;

import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
import com.example.springbootapp.auth.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Thymeleaf에서 메뉴 경로 접근 가능 여부를 판별 ({@code ${@menuAccess.allowed('/admin/codes')}}).
 */
@Component("menuAccess")
public class MenuAccessViewHelper {

	private final SessionAuthService sessionAuthService;

	public MenuAccessViewHelper(SessionAuthService sessionAuthService) {
		this.sessionAuthService = sessionAuthService;
	}

	public boolean allowed(String menuPath) {
		if (menuPath == null || menuPath.isBlank()) {
			return false;
		}
		HttpSession session = currentSession();
		if (session == null) {
			return true;
		}
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (login == null || login.getMenuAccess() == null) {
			return true;
		}
		return login.getMenuAccess().isMenuAllowed(MenuAccessSnapshot.normalizeMenuPath(menuPath));
	}

	private static HttpSession currentSession() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			return null;
		}
		HttpServletRequest request = attrs.getRequest();
		return request != null ? request.getSession(false) : null;
	}
}
