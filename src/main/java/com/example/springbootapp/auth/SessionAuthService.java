package com.example.springbootapp.auth;

import com.example.springbootapp.domain.User;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 로그인/로그아웃 시 HTTP 세션 값 설정.
 * <ul>
 *   <li>{@value #ATTR_LOGIN_USER} — {@link LoginSession} 객체</li>
 * </ul>
 */
@Service
public class SessionAuthService {

    /** 세션에 저장되는 로그인 사용자 객체 키 */
    public static final String ATTR_LOGIN_USER = "loginUser";

    @Value("${app.auth.session-timeout-minutes:10}")
    private int sessionTimeoutMinutes;

    public int getSessionTimeoutSeconds() {
        return Math.max(1, sessionTimeoutMinutes) * 60;
    }

    public int getSessionTimeoutMinutes() {
        return Math.max(1, sessionTimeoutMinutes);
    }

    public void loginFromUser(HttpSession session, User user) {
        LoginSession loginSession = new LoginSession();
        loginSession.setUserId(user.getId());
        loginSession.setUserName(user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getId());
        loginSession.setEmail(user.getEmail());
        loginSession.setLoginType("FORM");
        establishSession(session, loginSession);
    }

    public void loginFromKakao(HttpSession session, Map<String, Object> kakaoUser) {
        LoginSession loginSession = new LoginSession();
        loginSession.setUserId(resolveKakaoUserId(kakaoUser));
        loginSession.setUserName(resolveKakaoNickname(kakaoUser));
        loginSession.setEmail(resolveKakaoEmail(kakaoUser));
        loginSession.setLoginType("KAKAO");
        establishSession(session, loginSession);
    }

    public void loginFromNaver(HttpSession session, Map<String, Object> naverBody) {
        LoginSession loginSession = new LoginSession();
        loginSession.setUserId(resolveNaverUserId(naverBody));
        loginSession.setUserName(resolveNaverDisplayName(naverBody));
        loginSession.setEmail(resolveNaverEmail(naverBody));
        loginSession.setLoginType("NAVER");
        establishSession(session, loginSession);
    }

    private void establishSession(HttpSession session, LoginSession loginSession) {
        loginSession.setLoginAt(LocalDateTime.now());
        session.setAttribute(ATTR_LOGIN_USER, loginSession);
        session.setMaxInactiveInterval(getSessionTimeoutSeconds());
    }

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

    public boolean isLoggedIn(HttpSession session) {
        return getLoginSession(session) != null;
    }

    public String getLoginUserId(HttpSession session) {
        LoginSession loginSession = getLoginSession(session);
        return loginSession != null ? loginSession.getUserId() : null;
    }

    public void logout(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(ATTR_LOGIN_USER);
        session.invalidate();
    }

    public static String resolveKakaoUserId(Map<String, Object> kakaoUser) {
        if (kakaoUser == null || kakaoUser.get("id") == null) {
            return "kakao:unknown";
        }
        return "kakao:" + kakaoUser.get("id");
    }

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

    public static String resolveNaverUserId(Map<String, Object> naverBody) {
        Map<String, Object> profile = resolveNaverProfile(naverBody);
        Object id = profile.get("id");
        if (id == null || id.toString().isBlank()) {
            return "naver:unknown";
        }
        return "naver:" + id;
    }

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

    public static String resolveNaverEmail(Map<String, Object> naverBody) {
        Map<String, Object> profile = resolveNaverProfile(naverBody);
        Object email = profile.get("email");
        if (email != null && !email.toString().isBlank()) {
            return email.toString();
        }
        return null;
    }
}
