package com.example.springbootapp.controller;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.AuthRequiredPaths;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.service.UserAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/**
 * 인증(로그인·로그아웃) 컨트롤러.
 * <p>기본 경로: {@code /auth}</p>
 */
@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserMapper userMapper;
    private final SessionAuthService sessionAuthService;
    private final UserAccessLogService userAccessLogService;
    public AuthController(
            UserMapper userMapper,
            SessionAuthService sessionAuthService,
            UserAccessLogService userAccessLogService) {
        this.userMapper = userMapper;
        this.sessionAuthService = sessionAuthService;
        this.userAccessLogService = userAccessLogService;
    }
    /**
     * 로그인 페이지로 리다이렉트합니다.
     *
     * @return out: {@code redirect:/pages/authentication/simple/login}
     */
    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/pages/authentication/simple/login";
    }
    /**
     * 폼 로그인을 처리합니다.
     *
     * @param id                 in: 사용자 아이디
     * @param pw                 in: 비밀번호
     * @param request            in: HTTP 요청 (세션·접속 로그용)
     * @param redirectAttributes in: 리다이렉트 시 플래시 메시지 전달용
     * @return out: 성공 시 {@code redirect:/dashboard}, 실패 시 {@code redirect:/pages/authentication/simple/login}
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("id") String id,
            @RequestParam("pw") String pw,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        String trimmedId = id != null ? id.trim() : "";
        if (trimmedId.isEmpty() || pw == null || pw.isBlank()) {
            userAccessLogService.recordLogin(request, null, "FORM", false, "입력값 없음");
            redirectAttributes.addFlashAttribute("loginError", "아이디와 비밀번호를 입력하세요.");
            return "redirect:/pages/authentication/simple/login";
        }
        Optional<User> user = userMapper.findById(trimmedId);
        if (user.isEmpty() || !pw.equals(user.get().getPw())) {
            userAccessLogService.recordLogin(request, user.orElse(null), "FORM", false, "인증 실패");
            redirectAttributes.addFlashAttribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/pages/authentication/simple/login";
        }
        sessionAuthService.loginFromUser(request.getSession(true), user.get(), request);
        redirectAttributes.addFlashAttribute("loginSuccess", user.get().getName() + "님, 로그인되었습니다.");
        if (AuthRequiredPaths.isSafeReturnUrl(returnUrl)) {
            return "redirect:" + returnUrl.trim();
        }
        return "redirect:/dashboard";
    }
    /**
     * GET 방식으로 로그아웃을 처리합니다.
     *
     * @param request            in: HTTP 요청 (세션 무효화용)
     * @param redirectAttributes in: 리다이렉트 시 플래시 메시지 전달용
     * @return out: {@code redirect:/pages/authentication/simple/login}
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        sessionAuthService.logout(request.getSession(false), request);
        redirectAttributes.addFlashAttribute("logoutSuccess", true);
        return "redirect:/pages/authentication/simple/login";
    }
    /**
     * POST 방식으로 로그아웃을 처리합니다.
     *
     * @param request            in: HTTP 요청 (세션 무효화용)
     * @param redirectAttributes in: 리다이렉트 시 플래시 메시지 전달용
     * @return out: {@code redirect:/pages/authentication/simple/login}
     */
    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return logout(request, redirectAttributes);
    }
}
