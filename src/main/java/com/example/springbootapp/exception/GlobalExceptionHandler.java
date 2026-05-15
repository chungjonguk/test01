package com.example.springbootapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(basePackages = "com.example.springbootapp.controller")
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public String handleIllegalArgument(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
		log.warn("요청 검증 실패: {}", ex.getMessage());
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/users";
	}

	@ExceptionHandler(IllegalStateException.class)
	public String handleIllegalState(IllegalStateException ex, RedirectAttributes redirectAttributes) {
		log.error("처리 중 오류: {}", ex.getMessage());
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/users";
	}
}
