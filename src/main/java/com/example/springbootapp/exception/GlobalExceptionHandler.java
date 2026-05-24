package com.example.springbootapp.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
	public Object handleIllegalArgument(
			IllegalArgumentException ex,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		log.warn("요청 검증 실패: {}", ex.getMessage());
		if (isApiRequest(request)) {
			return apiError(HttpStatus.BAD_REQUEST, ex.getMessage());
		}
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/users";
	}
	@ExceptionHandler(IllegalStateException.class)
	public Object handleIllegalState(
			IllegalStateException ex,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		log.error("처리 중 오류: {}", ex.getMessage());
		if (isApiRequest(request)) {
			return apiError(HttpStatus.CONFLICT, ex.getMessage());
		}
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/users";
	}
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Object handleUnreadableJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
		log.warn("JSON 파싱 실패: {}", ex.getMessage());
		if (!isApiRequest(request)) {
			throw ex;
		}
		return apiError(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
	}
	@ExceptionHandler(DataIntegrityViolationException.class)
	public Object handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
		log.warn("DB 무결성 오류: {}", ex.getMostSpecificCause().getMessage());
		if (!isApiRequest(request)) {
			throw ex;
		}
		String message = "이미 존재하는 코드이거나 데이터 제약 조건에 맞지 않습니다.";
		String cause = ex.getMostSpecificCause().getMessage();
		if (cause != null && cause.contains("Duplicate entry")) {
			message = "동일한 코드그룹/상세코드가 이미 존재합니다. 상세코드 다건 저장 시 DB PK(code_id, code_val) 설정이 필요합니다.";
		}
		return apiError(HttpStatus.CONFLICT, message);
	}
	@ExceptionHandler(DataAccessException.class)
	public Object handleDataAccess(DataAccessException ex, HttpServletRequest request) {
		log.error("DB 처리 오류", ex);
		if (!isApiRequest(request)) {
			throw ex;
		}
		return apiError(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다.");
	}
	@ExceptionHandler(Exception.class)
	public Object handleGeneral(Exception ex, HttpServletRequest request) throws Exception {
		if (!isApiRequest(request)) {
			throw ex;
		}
		log.error("API 처리 오류: {} {}", request.getMethod(), request.getRequestURI(), ex);
		String message = ex.getMessage() != null ? ex.getMessage() : "요청 처리 중 오류가 발생했습니다.";
		return apiError(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}
	private static ResponseEntity<Map<String, Object>> apiError(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of("message", message));
	}
	private static boolean isApiRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri != null && uri.startsWith("/api/");
	}
}
