package com.example.springbootapp.controller;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.config.AuthRequiredPaths;
import com.example.springbootapp.config.web.PublicPathCryptoService;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.service.UserAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
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
    private static final String LOGIN_LOGICAL_PATH = "/pages/authentication/simple/login";

    private final UserMapper userMapper;
    private final SessionAuthService sessionAuthService;
    private final UserAccessLogService userAccessLogService;
    private final ObjectProvider<PublicPathCryptoService> publicPathCrypto;
    public AuthController(
            UserMapper userMapper,
            SessionAuthService sessionAuthService,
            UserAccessLogService userAccessLogService,
            ObjectProvider<PublicPathCryptoService> publicPathCrypto) {
        this.userMapper = userMapper;
        this.sessionAuthService = sessionAuthService;
        this.userAccessLogService = userAccessLogService;
        this.publicPathCrypto = publicPathCrypto;
    }

    /**
     * 로그인 페이지로 리다이렉트할 대상 경로를 반환합니다.
     * <p>경로 암호화가 켜져 있으면 공개(암호화) URL로 직접 리다이렉트하여
     * 2단계 리다이렉트로 인해 플래시 메시지(loginError 등)가 사라지는 것을 막습니다.</p>
     */
    private String loginRedirect() {
        PublicPathCryptoService crypto = publicPathCrypto.getIfAvailable();
        if (crypto != null && crypto.isEnabled()) {
            return "redirect:" + crypto.toPublicPath(LOGIN_LOGICAL_PATH);
        }
        return "redirect:" + LOGIN_LOGICAL_PATH;
    }
    /**
     * 로그인 페이지로 리다이렉트합니다.
     *
     * @return out: {@code redirect:/pages/authentication/simple/login}
     */
    @GetMapping("/login")
    public String loginPage() {
        return loginRedirect();
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
            redirectAttributes.addFlashAttribute("loginError",
                    "아이디(로그인 전화번호, 로그인 전용 아이디) 또는 비밀번호가 잘못 되었습니다.");
            return loginRedirect();
        }
        Optional<User> user = userMapper.findById(trimmedId);
        if (user.isEmpty() || !pw.equals(user.get().getPw())) {
            userAccessLogService.recordLogin(request, user.orElse(null), "FORM", false, "인증 실패");
            redirectAttributes.addFlashAttribute("loginError",
                    "아이디(로그인 전화번호, 로그인 전용 아이디) 또는 비밀번호가 잘못 되었습니다.");
            return loginRedirect();
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
        return loginRedirect();
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
