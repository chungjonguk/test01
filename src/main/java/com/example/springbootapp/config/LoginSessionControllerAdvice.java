package com.example.springbootapp.config;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
/**
 * 모든 화면에서 로그인 세션 값을 Model로 노출 (Thymeleaf: loginUser, isLoggedIn).
 */
@ControllerAdvice
public class LoginSessionControllerAdvice {
    private final SessionAuthService sessionAuthService;
    public LoginSessionControllerAdvice(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }
    @ModelAttribute("loginUser")
    public LoginSession loginUser(HttpSession session) {
        return sessionAuthService.getLoginSession(session);
    }
    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(HttpSession session) {
        return sessionAuthService.isLoggedIn(session);
    }
    @ModelAttribute("sessionTimeoutMinutes")
    public int sessionTimeoutMinutes() {
        return sessionAuthService.getSessionTimeoutMinutes();
    }
}
