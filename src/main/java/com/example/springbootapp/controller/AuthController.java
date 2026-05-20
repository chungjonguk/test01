package com.example.springbootapp.controller;

import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final SessionAuthService sessionAuthService;

    public AuthController(UserMapper userMapper, SessionAuthService sessionAuthService) {
        this.userMapper = userMapper;
        this.sessionAuthService = sessionAuthService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/pages/authentication/simple/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("id") String id,
            @RequestParam("pw") String pw,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        String trimmedId = id != null ? id.trim() : "";
        if (trimmedId.isEmpty() || pw == null || pw.isBlank()) {
            redirectAttributes.addFlashAttribute("loginError", "아이디와 비밀번호를 입력하세요.");
            return "redirect:/pages/authentication/simple/login";
        }

        Optional<User> user = userMapper.findById(trimmedId);
        if (user.isEmpty() || !pw.equals(user.get().getPw())) {
            redirectAttributes.addFlashAttribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/pages/authentication/simple/login";
        }

        sessionAuthService.loginFromUser(request.getSession(true), user.get());
        redirectAttributes.addFlashAttribute("loginSuccess", user.get().getName() + "님, 로그인되었습니다.");
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        sessionAuthService.logout(request.getSession(false));
        redirectAttributes.addFlashAttribute("logoutSuccess", true);
        return "redirect:/pages/authentication/simple/login";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return logout(request, redirectAttributes);
    }
}
