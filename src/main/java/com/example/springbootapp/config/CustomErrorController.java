package com.example.springbootapp.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Whitelabel 대신 Falcon 404/500 페이지를 표시합니다.
 */
@Controller
public class CustomErrorController implements ErrorController {

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request) {
		return resolveView(request);
	}

	@RequestMapping("/error/not-found")
	public String notFound() {
		return "error/404";
	}

	static String resolveView(HttpServletRequest request) {
		Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		int status = statusCode instanceof Integer code
				? code
				: HttpStatus.INTERNAL_SERVER_ERROR.value();

		if (status == HttpStatus.NOT_FOUND.value() || status == HttpStatus.FORBIDDEN.value()) {
			return "error/404";
		}
		if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
			return "pages/errors/500";
		}
		return status >= 500 ? "pages/errors/500" : "error/404";
	}
}
