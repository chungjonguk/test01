package com.example.springbootapp.config.web;



import java.io.IOException;

import org.springframework.context.annotation.Profile;

import org.springframework.core.Ordered;

import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;



/**

 * 평문·{@code .do} URL을 암호화 공개 URL({@code /e/.../*.do})로 리다이렉트합니다.

 */

@Component

@Profile("!test")

@Order(Ordered.HIGHEST_PRECEDENCE + 20)

public class DoPathRedirectFilter extends OncePerRequestFilter {



	private final PublicPathCryptoService publicPathCryptoService;



	public DoPathRedirectFilter(PublicPathCryptoService publicPathCryptoService) {

		this.publicPathCryptoService = publicPathCryptoService;

	}



	@Override

	protected void doFilterInternal(

			HttpServletRequest request,

			HttpServletResponse response,

			FilterChain filterChain) throws ServletException, IOException {

		if (!"GET".equalsIgnoreCase(request.getMethod())) {

			filterChain.doFilter(request, response);

			return;

		}

		String contextPath = request.getContextPath();

		String uri = request.getRequestURI();

		String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;

		if (path.isEmpty()) {

			path = "/";

		}

		path = DoPathHelper.sanitizeRequestPath(path);



		if (publicPathCryptoService.isEnabled()) {

			if (publicPathCryptoService.isPublicPath(path)) {

				filterChain.doFilter(request, response);

				return;

			}

			if (DoPathHelper.shouldSkipEncryption(path)) {

				filterChain.doFilter(request, response);

				return;

			}

			String logical = DoPathHelper.stripDoSuffix(path);

			String target = publicPathCryptoService.toPublicPath(logical);

			if (!target.equals(path)) {

				redirect(request, response, contextPath, target);

				return;

			}

			filterChain.doFilter(request, response);

			return;

		}



		if (DoPathHelper.shouldSkipRedirect(path)) {

			filterChain.doFilter(request, response);

			return;

		}

		String target = DoPathHelper.toDoPath(path);

		if (target.equals(path)) {

			filterChain.doFilter(request, response);

			return;

		}

		redirect(request, response, contextPath, target);

	}



	private static void redirect(

			HttpServletRequest request,

			HttpServletResponse response,

			String contextPath,

			String target) throws IOException {

		String query = request.getQueryString();

		String location = contextPath + target + (query != null ? "?" + query : "");

		response.sendRedirect(location);

	}

}


