package com.example.springbootapp.config;
import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
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

    /**
     * 로고/브랜드 클릭 시 이동할 공통 경로.
     * <p>로그인 상태면 대시보드, 비로그인 상태면 로그인 화면으로 보냅니다.</p>
     */
    @ModelAttribute("brandHomeUrl")
    public String brandHomeUrl(HttpSession session) {
        return sessionAuthService.isLoggedIn(session)
                ? "/dashboard"
                : "/pages/authentication/simple/login";
    }

    /** 플랫폼 관리자(전체 메뉴·플랫폼 전용 화면) 여부 */
    @ModelAttribute("platformAdmin")
    public boolean platformAdmin(HttpSession session) {
        LoginSession login = sessionAuthService.getLoginSession(session);
        if (login == null || login.getMenuAccess() == null) {
            return false;
        }
        MenuAccessSnapshot access = login.getMenuAccess();
        return access.isWriteAll() || access.role() == AppRole.PLATFORM_ADMIN;
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
