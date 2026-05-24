package com.example.springbootapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.domain.UserAccessLog;
import com.example.springbootapp.mapper.UserAccessLogMapper;
import com.example.springbootapp.util.ClientDeviceResolver;
import com.example.springbootapp.util.ClientDeviceResolver.ClientDeviceInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class UserAccessLogService {

	private static final Logger log = LoggerFactory.getLogger(UserAccessLogService.class);

	public static final String ACCESS_LOGIN = "LOGIN";
	public static final String ACCESS_LOGOUT = "LOGOUT";
	public static final String ACCESS_PAGE = "PAGE";

	private final UserAccessLogMapper userAccessLogMapper;

	public UserAccessLogService(UserAccessLogMapper userAccessLogMapper) {
		this.userAccessLogMapper = userAccessLogMapper;
	}

	@Transactional
	public void recordLogin(HttpServletRequest request, User user, String loginTypeCd, boolean success, String failReason) {
		UserAccessLog row = baseFromRequest(request, ACCESS_LOGIN);
		row.setLoginTypeCd(loginTypeCd);
		row.setSuccessYn(success ? "Y" : "N");
		row.setFailReason(failReason);
		if (user != null) {
			row.setUserId(user.getId());
			row.setUserNm(user.getName());
			row.setRegId(user.getId());
		} else if (request != null) {
			String attemptedId = request.getParameter("id");
			if (attemptedId != null && !attemptedId.isBlank()) {
				row.setUserId(attemptedId.trim());
			}
		}
		insertQuietly(row);
	}

	@Transactional
	public void recordLoginSession(HttpServletRequest request, LoginSession loginSession) {
		if (loginSession == null) {
			return;
		}
		UserAccessLog row = baseFromRequest(request, ACCESS_LOGIN);
		row.setUserId(loginSession.getUserId());
		row.setUserNm(loginSession.getUserName());
		row.setLoginTypeCd(loginSession.getLoginType());
		row.setSuccessYn("Y");
		row.setRegId(loginSession.getUserId());
		insertQuietly(row);
	}

	@Transactional
	public void recordLogout(HttpServletRequest request, LoginSession loginSession) {
		if (loginSession == null) {
			return;
		}
		UserAccessLog row = baseFromRequest(request, ACCESS_LOGOUT);
		row.setUserId(loginSession.getUserId());
		row.setUserNm(loginSession.getUserName());
		row.setLoginTypeCd(loginSession.getLoginType());
		row.setSuccessYn("Y");
		row.setRegId(loginSession.getUserId());
		insertQuietly(row);
	}

	@Transactional(readOnly = true)
	public List<UserAccessLog> findRecentByUserId(String userId, int limit) {
		if (userId == null || userId.isBlank()) {
			return List.of();
		}
		return userAccessLogMapper.findByUserId(userId, Math.max(1, Math.min(limit, 500)));
	}

	@Transactional(readOnly = true)
	public List<UserAccessLog> findByRange(LocalDateTime rangeStart, LocalDateTime rangeEnd, int limit) {
		return userAccessLogMapper.findByRange(rangeStart, rangeEnd, Math.max(1, Math.min(limit, 1000)));
	}

	@Transactional(readOnly = true)
	public List<UserAccessLog> searchForAdmin(
			String userId,
			String clientIp,
			String accessTypeCd,
			String loginTypeCd,
			String successYn,
			LocalDateTime rangeStart,
			LocalDateTime rangeEnd,
			int limit) {
		return userAccessLogMapper.search(
				trimToNull(userId),
				trimToNull(clientIp),
				trimToNull(accessTypeCd),
				trimToNull(loginTypeCd),
				trimToNull(successYn),
				rangeStart,
				rangeEnd,
				Math.max(1, Math.min(limit, 500)));
	}

	public static LocalDateTime parseDateTime(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String normalized = value.trim().replace(' ', 'T');
		if (normalized.length() == 10) {
			normalized += "T00:00:00";
		}
		try {
			return LocalDateTime.parse(normalized);
		} catch (Exception ex) {
			throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다: " + value);
		}
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		return t.isEmpty() ? null : t;
	}

	private UserAccessLog baseFromRequest(HttpServletRequest request, String accessTypeCd) {
		UserAccessLog row = new UserAccessLog();
		row.setAccessTypeCd(accessTypeCd);
		row.setAccessDt(LocalDateTime.now());
		row.setRegId("SYSTEM");
		if (request == null) {
			return row;
		}
		row.setRequestUri(trim(request.getRequestURI(), 500));
		row.setHttpMethod(trim(request.getMethod(), 10));
		row.setClientIp(ClientDeviceResolver.resolveClientIp(request));
		row.setUserAgent(trim(request.getHeader("User-Agent"), 500));
		ClientDeviceInfo device = ClientDeviceResolver.resolveDevice(request);
		row.setDeviceTypeCd(device.getDeviceTypeCd());
		row.setDeviceOs(device.getDeviceOs());
		row.setDeviceBrowser(device.getDeviceBrowser());
		row.setDeviceModel(device.getDeviceModel());
		HttpSession session = request.getSession(false);
		if (session != null) {
			row.setSessionId(trim(session.getId(), 64));
		}
		return row;
	}

	private void insertQuietly(UserAccessLog row) {
		try {
			userAccessLogMapper.insert(row);
		} catch (Exception ex) {
			log.warn("접속 이력 저장 실패: userId={}, type={}", row.getUserId(), row.getAccessTypeCd(), ex);
		}
	}

	private static String trim(String value, int maxLen) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		if (t.isEmpty()) {
			return null;
		}
		return t.length() <= maxLen ? t : t.substring(0, maxLen);
	}
}
