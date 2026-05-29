package com.example.springbootapp.web;

import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.AuthRequiredPaths;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import com.example.springbootapp.service.MenuAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 역할별 화면·API 접근 제어.
 */
@Profile("!test")
@Component
public class AuthPermissionInterceptor implements HandlerInterceptor {

	private final SessionAuthService sessionAuthService;
	private final MenuAccessService menuAccessService;
	private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;

	public AuthPermissionInterceptor(
			SessionAuthService sessionAuthService,
			MenuAccessService menuAccessService,
			ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
		this.sessionAuthService = sessionAuthService;
		this.menuAccessService = menuAccessService;
		this.publicPathCrypto = publicPathCrypto;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String uri = resolveUri(request);
		if (shouldSkip(uri)) {
			return true;
		}
		HttpSession session = request.getSession(false);
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (login == null) {
			if (uri.startsWith("/api/")) {
				if (AuthRequiredPaths.isPublicApi(uri)) {
					return true;
				}
				writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다. 다시 로그인해 주세요.");
				return false;
			}
			return true;
		}
		if (uri.startsWith("/api/")) {
			if (!menuAccessService.canAccessUri(login, uri)) {
				writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
				return false;
			}
			if (!isReadMethod(request.getMethod()) && !menuAccessService.canWriteApi(login, request.getMethod(), uri)) {
				writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
				return false;
			}
			return true;
		}
		if (!menuAccessService.canAccessUri(login, uri)) {
			String ctx = request.getContextPath();
			String logicalUri = uri;
			PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
			if (crypto != null && crypto.isEnabled()) {
				logicalUri = crypto.resolveLogicalPath(request);
			}
			String errorPath = "/pages/errors/403";
			if (crypto != null && crypto.isEnabled()) {
				errorPath = crypto.toPublicPath(errorPath);
			}
			String detail = "이 화면에 대한 접근 권한이 없습니다.";
			if (logicalUri != null && logicalUri.startsWith("/admin/user-access-logs")) {
				detail = "접속 로그는 플랫폼 관리자만 이용할 수 있습니다.";
			}
			String msg = URLEncoder.encode(detail, StandardCharsets.UTF_8);
			response.sendRedirect(ctx + errorPath + "?msg=" + msg);
			return false;
		}
		return true;
	}

	private static void writeJsonError(HttpServletResponse response, int status, String message) throws java.io.IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		String safe = message == null ? "" : message.replace("\\", "\\\\").replace("\"", "\\\"");
		response.getWriter().write("{\"success\":false,\"message\":\"" + safe + "\"}");
	}

	private boolean isReadMethod(String method) {
		if (method == null) {
			return true;
		}
		String m = method.toUpperCase();
		return "GET".equals(m) || "HEAD".equals(m) || "OPTIONS".equals(m);
	}

	private String resolveUri(HttpServletRequest request) {
		PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
		if (crypto != null && crypto.isEnabled()) {
			return crypto.resolveLogicalPath(request);
		}
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		return uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
	}

	private boolean shouldSkip(String uri) {
		return uri.startsWith("/auth/")
				|| uri.startsWith("/assets/")
				|| uri.startsWith("/vendors/")
				|| uri.startsWith("/api/url/")
				|| uri.contains("/favicon")
				|| uri.startsWith("/error");
	}
}
