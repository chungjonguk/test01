package com.example.springbootapp.auth;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.service.UserAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
/**
 * 로그인/로그아웃 시 HTTP 세션 값 설정 및 소셜 로그인 사용자 정보 해석.
 * <ul>
 *   <li>{@value #ATTR_LOGIN_USER} — {@link LoginSession} 객체</li>
 * </ul>
 */
@Service
public class SessionAuthService {
    /** 세션에 저장되는 로그인 사용자 객체 키 */
    public static final String ATTR_LOGIN_USER = "loginUser";
    private final UserAccessLogService userAccessLogService;
    @Value("${app.auth.session-timeout-minutes:10}")
    private int sessionTimeoutMinutes;
    public SessionAuthService(UserAccessLogService userAccessLogService) {
        this.userAccessLogService = userAccessLogService;
    }
    /**
     * 설정된 세션 타임아웃을 초 단위로 반환한다.
     *
     * @return 세션 비활성 간격(초)
     */
    public int getSessionTimeoutSeconds() {
        return Math.max(1, sessionTimeoutMinutes) * 60;
    }
    /**
     * 설정된 세션 타임아웃을 분 단위로 반환한다.
     *
     * @return 세션 비활성 간격(분)
     */
    public int getSessionTimeoutMinutes() {
        return Math.max(1, sessionTimeoutMinutes);
    }
    /**
     * 일반(폼) 로그인 사용자 정보로 세션을 생성한다.
     *
     * @param session HTTP 세션
     * @param user    로그인한 사용자
     */
    public void loginFromUser(HttpSession session, User user) {
        loginFromUser(session, user, null);
    }
    /**
     * 일반(폼) 로그인 사용자 정보로 세션을 생성하고 접속 이력을 기록한다.
     *
     * @param session HTTP 세션
     * @param user    로그인한 사용자
     * @param request HTTP 요청 (접속 이력 기록용, null 허용)
     */
    public void loginFromUser(HttpSession session, User user, HttpServletRequest request) {
        LoginSession loginSession = new LoginSession();
        loginSession.setUserId(user.getId());
        loginSession.setUserName(user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getId());
        loginSession.setEmail(user.getEmail());
        loginSession.setLoginType("FORM");
        establishSession(session, loginSession);
        userAccessLogService.recordLoginSession(request, loginSession);
    }
    /**
     * 카카오 로그인 사용자 정보로 세션을 생성한다.
     *
     * @param session   HTTP 세션
     * @param kakaoUser 카카오 사용자 API 응답 맵
     */
    public void loginFromKakao(HttpSession session, Map<String, Object> kakaoUser) {
        loginFromKakao(session, kakaoUser, null);
    }
    /**
     * 카카오 로그인 사용자 정보로 세션을 생성하고 접속 이력을 기록한다.
     *
     * @param session   HTTP 세션
     * @param kakaoUser 카카오 사용자 API 응답 맵
     * @param request   HTTP 요청 (접속 이력 기록용, null 허용)
     */
    public void loginFromKakao(HttpSession session, Map<String, Object> kakaoUser, HttpServletRequest request) {
        LoginSession loginSession = new LoginSession();
        loginSession.setUserId(resolveKakaoUserId(kakaoUser));
        loginSession.setUserName(resolveKakaoNickname(kakaoUser));
        loginSession.setEmail(resolveKakaoEmail(kakaoUser));
        loginSession.setLoginType("KAKAO");
        establishSession(session, loginSession);
        userAccessLogService.recordLoginSession(request, loginSession);
    }
    /**
     * 네이버 로그인 사용자 정보로 세션을 생성한다.
     *
     * @param session    HTTP 세션
     * @param naverBody  네이버 사용자 API 응답 본문
     */
    public void loginFromNaver(HttpSession session, Map<String, Object> naverBody) {
        loginFromNaver(session, naverBody, null);
    }
    /**
     * 네이버 로그인 사용자 정보로 세션을 생성하고 접속 이력을 기록한다.
     *
     * @param session    HTTP 세션
     * @param naverBody  네이버 사용자 API 응답 본문
     * @param request    HTTP 요청 (접속 이력 기록용, null 허용)
     */
    public void loginFromNaver(HttpSession session, Map<String, Object> naverBody, HttpServletRequest request) {
        LoginSession loginSession = new LoginSession();
        loginSession.setUserId(resolveNaverUserId(naverBody));
        loginSession.setUserName(resolveNaverDisplayName(naverBody));
        loginSession.setEmail(resolveNaverEmail(naverBody));
        loginSession.setLoginType("NAVER");
        establishSession(session, loginSession);
        userAccessLogService.recordLoginSession(request, loginSession);
    }
    private void establishSession(HttpSession session, LoginSession loginSession) {
        loginSession.setLoginAt(LocalDateTime.now());
        session.setAttribute(ATTR_LOGIN_USER, loginSession);
        session.setMaxInactiveInterval(getSessionTimeoutSeconds());
    }
    /**
     * 세션에서 로그인 사용자 정보를 조회한다.
     *
     * @param session HTTP 세션
     * @return 로그인 세션 정보, 미로그인 시 null
     */
    public LoginSession getLoginSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTR_LOGIN_USER);
        if (value instanceof LoginSession loginSession) {
            return loginSession;
        }
        return null;
    }
    /**
     * 로그인 여부를 확인한다.
     *
     * @param session HTTP 세션
     * @return 로그인되어 있으면 true
     */
    public boolean isLoggedIn(HttpSession session) {
        return getLoginSession(session) != null;
    }
    /**
     * 로그인 사용자 ID를 반환한다.
     *
     * @param session HTTP 세션
     * @return 사용자 ID, 미로그인 시 null
     */
    public String getLoginUserId(HttpSession session) {
        LoginSession loginSession = getLoginSession(session);
        return loginSession != null ? loginSession.getUserId() : null;
    }
    /**
     * 세션에서 로그인 정보를 제거하고 세션을 무효화한다.
     *
     * @param session HTTP 세션
     */
    public void logout(HttpSession session) {
        logout(session, null);
    }
    /**
     * 로그아웃 접속 이력을 기록한 뒤 세션을 무효화한다.
     *
     * @param session HTTP 세션
     * @param request HTTP 요청 (접속 이력 기록용, null 허용)
     */
    public void logout(HttpSession session, HttpServletRequest request) {
        if (session == null) {
            return;
        }
        LoginSession loginSession = getLoginSession(session);
        if (loginSession != null) {
            userAccessLogService.recordLogout(request, loginSession);
        }
        session.removeAttribute(ATTR_LOGIN_USER);
        session.invalidate();
    }
    /**
     * 카카오 API 응답에서 내부 사용자 ID를 생성한다.
     *
     * @param kakaoUser 카카오 사용자 API 응답 맵
     * @return kakao:{id} 형식의 사용자 ID
     */
    public static String resolveKakaoUserId(Map<String, Object> kakaoUser) {
        if (kakaoUser == null || kakaoUser.get("id") == null) {
            return "kakao:unknown";
        }
        return "kakao:" + kakaoUser.get("id");
    }
    /**
     * 카카오 API 응답에서 표시용 닉네임을 추출한다.
     *
     * @param kakaoUser 카카오 사용자 API 응답 맵
     * @return 닉네임 또는 기본 표시명
     */
    @SuppressWarnings("unchecked")
    public static String resolveKakaoNickname(Map<String, Object> kakaoUser) {
        if (kakaoUser == null) {
            return "카카오 사용자";
        }
        Object props = kakaoUser.get("properties");
        if (props instanceof Map<?, ?> properties) {
            Object nickname = properties.get("nickname");
            if (nickname != null && !nickname.toString().isBlank()) {
                return nickname.toString();
            }
        }
        Object id = kakaoUser.get("id");
        return id != null ? "카카오 " + id : "카카오 사용자";
    }
    /**
     * 카카오 API 응답에서 이메일을 추출한다.
     *
     * @param kakaoUser 카카오 사용자 API 응답 맵
     * @return 이메일, 없으면 null
     */
    @SuppressWarnings("unchecked")
    public static String resolveKakaoEmail(Map<String, Object> kakaoUser) {
        if (kakaoUser == null) {
            return null;
        }
        Object account = kakaoUser.get("kakao_account");
        if (account instanceof Map<?, ?> kakaoAccount) {
            Object email = kakaoAccount.get("email");
            if (email != null && !email.toString().isBlank()) {
                return email.toString();
            }
        }
        return null;
    }
    /**
     * 네이버 API 응답 본문에서 profile(response) 맵을 추출한다.
     *
     * @param naverBody 네이버 사용자 API 응답 본문
     * @return 프로필 맵, 없으면 빈 맵
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> resolveNaverProfile(Map<String, Object> naverBody) {
        if (naverBody == null) {
            return Map.of();
        }
        Object response = naverBody.get("response");
        if (response instanceof Map<?, ?> profile) {
            return (Map<String, Object>) profile;
        }
        return Map.of();
    }
    /**
     * 네이버 API 응답에서 내부 사용자 ID를 생성한다.
     *
     * @param naverBody 네이버 사용자 API 응답 본문
     * @return naver:{id} 형식의 사용자 ID
     */
    public static String resolveNaverUserId(Map<String, Object> naverBody) {
        Map<String, Object> profile = resolveNaverProfile(naverBody);
        Object id = profile.get("id");
        if (id == null || id.toString().isBlank()) {
            return "naver:unknown";
        }
        return "naver:" + id;
    }
    /**
     * 네이버 API 응답에서 표시용 이름을 추출한다.
     *
     * @param naverBody 네이버 사용자 API 응답 본문
     * @return 이름·닉네임 또는 기본 표시명
     */
    public static String resolveNaverDisplayName(Map<String, Object> naverBody) {
        Map<String, Object> profile = resolveNaverProfile(naverBody);
        Object name = profile.get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        Object nickname = profile.get("nickname");
        if (nickname != null && !nickname.toString().isBlank()) {
            return nickname.toString();
        }
        Object id = profile.get("id");
        return id != null ? "네이버 " + id : "네이버 사용자";
    }
    /**
     * 네이버 API 응답에서 이메일을 추출한다.
     *
     * @param naverBody 네이버 사용자 API 응답 본문
     * @return 이메일, 없으면 null
     */
    public static String resolveNaverEmail(Map<String, Object> naverBody) {
        Map<String, Object> profile = resolveNaverProfile(naverBody);
        Object email = profile.get("email");
        if (email != null && !email.toString().isBlank()) {
            return email.toString();
        }
        return null;
    }
}
