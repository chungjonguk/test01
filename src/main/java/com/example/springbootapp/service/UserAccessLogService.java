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
/**
 * 사용자 로그인·로그아웃 및 접속 이력 기록·조회를 처리하는 서비스.
 */
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
	/**
	 * 로그인 시도 결과를 접속 이력에 기록한다.
	 *
	 * @param request      HTTP 요청 (IP·User-Agent 등 추출용)
	 * @param user         로그인 성공 시 사용자, 실패 시 null
	 * @param loginTypeCd  로그인 유형 코드
	 * @param success      성공 여부
	 * @param failReason   실패 사유 (성공 시 null)
	 */
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
	/**
	 * 로그인 세션 정보로 성공 접속 이력을 기록한다.
	 *
	 * @param request      HTTP 요청
	 * @param loginSession 로그인 세션 정보
	 */
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
	/**
	 * 로그아웃 접속 이력을 기록한다.
	 *
	 * @param request      HTTP 요청
	 * @param loginSession 로그아웃하는 로그인 세션 정보
	 */
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
	/**
	 * 로그인 사용자의 화면 접속(PAGE) 이력을 기록한다.
	 *
	 * @param request      HTTP 요청
	 * @param loginSession 로그인 세션 정보
	 */
	@Transactional
	public void recordPage(HttpServletRequest request, LoginSession loginSession) {
		if (loginSession == null) {
			return;
		}
		UserAccessLog row = baseFromRequest(request, ACCESS_PAGE);
		row.setUserId(loginSession.getUserId());
		row.setUserNm(loginSession.getUserName());
		row.setLoginTypeCd(loginSession.getLoginType());
		row.setSuccessYn("Y");
		row.setRegId(loginSession.getUserId());
		insertQuietly(row);
	}
	/**
	 * 사용자 ID별 최근 접속 이력을 조회한다.
	 *
	 * @param userId 사용자 ID
	 * @param limit  최대 조회 건수 (1~500)
	 * @return 접속 이력 목록
	 */
	@Transactional(readOnly = true)
	public List<UserAccessLog> findRecentByUserId(String userId, int limit) {
		if (userId == null || userId.isBlank()) {
			return List.of();
		}
		return userAccessLogMapper.findByUserId(userId, Math.max(1, Math.min(limit, 500)));
	}
	/**
	 * 기간 내 접속 이력을 조회한다.
	 *
	 * @param rangeStart 조회 시작 일시
	 * @param rangeEnd   조회 종료 일시
	 * @param limit      최대 조회 건수 (1~1000)
	 * @return 접속 이력 목록
	 */
	@Transactional(readOnly = true)
	public List<UserAccessLog> findByRange(LocalDateTime rangeStart, LocalDateTime rangeEnd, int limit) {
		return userAccessLogMapper.findByRange(rangeStart, rangeEnd, Math.max(1, Math.min(limit, 1000)));
	}
	/**
	 * 관리자 화면용 접속 이력을 조건 검색한다.
	 *
	 * @param userId       사용자 ID (null 허용)
	 * @param clientIp     클라이언트 IP (null 허용)
	 * @param accessTypeCd 접속 유형 코드 (null 허용)
	 * @param loginTypeCd  로그인 유형 코드 (null 허용)
	 * @param successYn    성공 여부 Y/N (null 허용)
	 * @param rangeStart   조회 시작 일시 (null 허용)
	 * @param rangeEnd     조회 종료 일시 (null 허용)
	 * @param limit        최대 조회 건수 (1~500)
	 * @return 접속 이력 목록
	 */
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
	/**
	 * 날짜·시간 문자열을 {@link LocalDateTime}으로 파싱한다.
	 *
	 * @param value 날짜·시간 문자열 (yyyy-MM-dd 또는 ISO 형식)
	 * @return 파싱된 일시, 빈 값이면 null
	 */
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
			log.warn("접속 이력 저장 실패: userId={}, type={}, uri={}, cause={}",
					row.getUserId(), row.getAccessTypeCd(), row.getRequestUri(), ex.getMessage());
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
