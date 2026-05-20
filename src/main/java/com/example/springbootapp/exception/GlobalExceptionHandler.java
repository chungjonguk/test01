package com.example.springbootapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Object handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
		log.debug("존재하지 않는 경로: {} {}", request.getMethod(), request.getRequestURI());
		if (isApiRequest(request)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Not Found", "path", request.getRequestURI()));
		}
		return "error/404";
	}

	@ExceptionHandler(ResourceAccessException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Object handleConnectionFailure(ResourceAccessException ex, HttpServletRequest request) {
		log.warn("외부 사이트 연결 실패: {} — {}", request.getRequestURI(), ex.getMessage());
		if (isApiRequest(request)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Connection failed", "message", ex.getMessage()));
		}
		return "error/404";
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public String handleIllegalArgument(
			IllegalArgumentException ex,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		log.warn("요청 검증 실패: {}", ex.getMessage());
		if (isApiRequest(request)) {
			throw ex;
		}
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/users";
	}

	@ExceptionHandler(IllegalStateException.class)
	public String handleIllegalState(
			IllegalStateException ex,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		log.error("처리 중 오류: {}", ex.getMessage());
		if (isApiRequest(request)) {
			throw ex;
		}
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/users";
	}

	private static boolean isApiRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri != null && uri.startsWith("/api/");
	}
}
