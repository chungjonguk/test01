package com.example.springbootapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Falcon 정적 HTML에서 온 상대 링크({@code ../../../app/...})가
 * {@code .../something.html} 페이지에서 열리면 {@code /app/app/...} 로 잘못 해석되는 경우가 있어
 * 한 단계 중복된 경로를 바로잡습니다.
 */
@Controller
public class LegacyPathRedirectController {

	@RequestMapping("/app/app/**")
	public String redirectDoubleAppPrefix(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String fixed = uri.replaceFirst("/app/app/", "/app/");
		String q = request.getQueryString();
		return "redirect:" + fixed + (q != null ? "?" + q : "");
	}
}
