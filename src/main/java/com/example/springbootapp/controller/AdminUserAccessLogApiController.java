package com.example.springbootapp.controller;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.domain.UserAccessLog;
import com.example.springbootapp.service.UserAccessLogService;
/**
 * 관리자 사용자 접속 이력 REST API.
 * <p>기본 경로: {@code /api/admin/user-access-logs}</p>
 */
@RestController
@RequestMapping("/api/admin/user-access-logs")
public class AdminUserAccessLogApiController {
	private final UserAccessLogService userAccessLogService;
	public AdminUserAccessLogApiController(UserAccessLogService userAccessLogService) {
		this.userAccessLogService = userAccessLogService;
	}
	/**
	 * 사용자 접속 이력을 조건에 따라 검색합니다.
	 *
	 * @param userId     in: 사용자 ID (선택)
	 * @param clientIp   in: 클라이언트 IP (선택)
	 * @param accessType in: 접속 유형 코드 (선택)
	 * @param loginType  in: 로그인 유형 코드 (선택)
	 * @param successYn  in: 성공 여부 Y/N (선택)
	 * @param start      in: 조회 시작 일시 (선택, 미입력 시 30일 전)
	 * @param end        in: 조회 종료 일시 (선택, 미입력 시 내일 0시)
	 * @param limit      in: 최대 조회 건수 (기본 200)
	 * @return out: {@code ResponseEntity<Map>} — {@code logs}, {@code count} 또는 400 시 {@code success}, {@code message}
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String userId,
			@RequestParam(required = false) String clientIp,
			@RequestParam(required = false) String accessType,
			@RequestParam(required = false) String loginType,
			@RequestParam(required = false) String successYn,
			@RequestParam(required = false) String start,
			@RequestParam(required = false) String end,
			@RequestParam(defaultValue = "200") int limit) {
		try {
			LocalDateTime rangeStart = resolveRangeStart(start);
			LocalDateTime rangeEnd = resolveRangeEnd(end);
			List<Map<String, Object>> logs = userAccessLogService
					.searchForAdmin(userId, clientIp, accessType, loginType, successYn, rangeStart, rangeEnd, limit)
					.stream()
					.map(this::toDto)
					.collect(Collectors.toList());
			Map<String, Object> body = new HashMap<>();
			body.put("logs", logs);
			body.put("count", logs.size());
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", false);
			body.put("message", ex.getMessage());
			body.put("logs", List.of());
			body.put("count", 0);
			return ResponseEntity.badRequest().body(body);
		}
	}
	private LocalDateTime resolveRangeStart(String start) {
		if (start != null && !start.isBlank()) {
			return UserAccessLogService.parseDateTime(start);
		}
		return LocalDateTime.now().minusDays(30);
	}
	private LocalDateTime resolveRangeEnd(String end) {
		if (end != null && !end.isBlank()) {
			LocalDateTime parsed = UserAccessLogService.parseDateTime(end);
			return parsed.toLocalDate().plusDays(1).atStartOfDay();
		}
		return LocalDateTime.now().plusDays(1);
	}
	private Map<String, Object> toDto(UserAccessLog log) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("accessId", log.getAccessId());
		row.put("userId", log.getUserId());
		row.put("userNm", log.getUserNm());
		row.put("accessTypeCd", log.getAccessTypeCd());
		row.put("loginTypeCd", log.getLoginTypeCd());
		row.put("successYn", log.getSuccessYn());
		row.put("requestUri", log.getRequestUri());
		row.put("httpMethod", log.getHttpMethod());
		row.put("clientIp", log.getClientIp());
		row.put("deviceTypeCd", log.getDeviceTypeCd());
		row.put("deviceOs", log.getDeviceOs());
		row.put("deviceBrowser", log.getDeviceBrowser());
		row.put("deviceModel", log.getDeviceModel());
		row.put("userAgent", log.getUserAgent());
		row.put("sessionId", log.getSessionId());
		row.put("failReason", log.getFailReason());
		row.put("accessDt", log.getAccessDt() != null ? log.getAccessDt().toString() : null);
		row.put("regId", log.getRegId());
		return row;
	}
}
