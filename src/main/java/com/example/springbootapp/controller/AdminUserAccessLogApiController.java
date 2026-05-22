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

@RestController
@RequestMapping("/api/admin/user-access-logs")
public class AdminUserAccessLogApiController {

	private final UserAccessLogService userAccessLogService;

	public AdminUserAccessLogApiController(UserAccessLogService userAccessLogService) {
		this.userAccessLogService = userAccessLogService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String userId,
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
					.searchForAdmin(userId, accessType, loginType, successYn, rangeStart, rangeEnd, limit)
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
		row.put("userAgent", log.getUserAgent());
		row.put("sessionId", log.getSessionId());
		row.put("failReason", log.getFailReason());
		row.put("accessDt", log.getAccessDt() != null ? log.getAccessDt().toString() : null);
		row.put("regId", log.getRegId());
		return row;
	}
}
