package com.example.springbootapp.config;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.service.LoginAuthDisplayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
/**
 * 모든 화면에서 로그인 세션 값을 Model로 노출 (Thymeleaf: loginUser, isLoggedIn).
 */
@ControllerAdvice
public class LoginSessionControllerAdvice {
    private final SessionAuthService sessionAuthService;
    private final LoginAuthDisplayService loginAuthDisplayService;

    public LoginSessionControllerAdvice(
            SessionAuthService sessionAuthService,
            LoginAuthDisplayService loginAuthDisplayService) {
        this.sessionAuthService = sessionAuthService;
        this.loginAuthDisplayService = loginAuthDisplayService;
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

    @ModelAttribute
    public void userAuthDropdown(HttpSession session, Model model) {
        loginAuthDisplayService.enrichUserDropdown(model, sessionAuthService.getLoginSession(session));
    }
}
