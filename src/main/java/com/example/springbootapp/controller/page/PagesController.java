package com.example.springbootapp.controller.page;

import com.example.springbootapp.auth.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PagesController {

    private final SessionAuthService sessionAuthService;

    public PagesController(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    // Pages - Starter
    @GetMapping("/pages/starter")
    public String pagesStarter(Model model) {
        model.addAttribute("title", "시작 페이지");
        return "pages/starter";
    }

    // Pages - Landing
    @GetMapping("/pages/landing")
    public String pagesLanding(Model model) {
        model.addAttribute("title", "랜딩");
        return "pages/landing";
    }

    // Pages - Authentication - Simple
    @GetMapping("/pages/authentication/simple/login")
    public String pagesAuthSimpleLogin(Model model) {
        model.addAttribute("title", "로그인");
        return "pages/authentication/simple/login";
    }

    @GetMapping("/pages/authentication/simple/logout")
    public String pagesAuthSimpleLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return performLogout(request, redirectAttributes);
    }

    @GetMapping("/pages/authentication/simple/register")
    public String pagesAuthSimpleRegister(Model model) {
        model.addAttribute("title", "회원가입");
        return "pages/authentication/simple/register";
    }

    @GetMapping("/pages/authentication/simple/forgot-password")
    public String pagesAuthSimpleForgotPassword(Model model) {
        model.addAttribute("title", "비밀번호 찾기");
        return "pages/authentication/simple/forgot-password";
    }

    @GetMapping("/pages/authentication/simple/confirm-mail")
    public String pagesAuthSimpleConfirmMail(Model model) {
        model.addAttribute("title", "메일 확인");
        return "pages/authentication/simple/confirm-mail";
    }

    @GetMapping("/pages/authentication/simple/reset-password")
    public String pagesAuthSimpleResetPassword(Model model) {
        model.addAttribute("title", "비밀번호 재설정");
        return "pages/authentication/simple/reset-password";
    }

    @GetMapping("/pages/authentication/simple/lock-screen")
    public String pagesAuthSimpleLockScreen(Model model) {
        model.addAttribute("title", "화면 잠금");
        return "pages/authentication/simple/lock-screen";
    }

    // Pages - Authentication - Card
    @GetMapping("/pages/authentication/card/login")
    public String pagesAuthCardLogin(Model model) {
        model.addAttribute("title", "로그인");
        return "pages/authentication/card/login";
    }

    @GetMapping("/pages/authentication/card/logout")
    public String pagesAuthCardLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return performLogout(request, redirectAttributes);
    }

    @GetMapping("/pages/authentication/card/register")
    public String pagesAuthCardRegister(Model model) {
        model.addAttribute("title", "회원가입");
        return "pages/authentication/card/register";
    }

    @GetMapping("/pages/authentication/card/forgot-password")
    public String pagesAuthCardForgotPassword(Model model) {
        model.addAttribute("title", "비밀번호 찾기");
        return "pages/authentication/card/forgot-password";
    }

    @GetMapping("/pages/authentication/card/confirm-mail")
    public String pagesAuthCardConfirmMail(Model model) {
        model.addAttribute("title", "메일 확인");
        return "pages/authentication/card/confirm-mail";
    }

    @GetMapping("/pages/authentication/card/reset-password")
    public String pagesAuthCardResetPassword(Model model) {
        model.addAttribute("title", "비밀번호 재설정");
        return "pages/authentication/card/reset-password";
    }

    @GetMapping("/pages/authentication/card/lock-screen")
    public String pagesAuthCardLockScreen(Model model) {
        model.addAttribute("title", "화면 잠금");
        return "pages/authentication/card/lock-screen";
    }

    // Pages - Authentication - Split
    @GetMapping("/pages/authentication/split/login")
    public String pagesAuthSplitLogin(Model model) {
        model.addAttribute("title", "로그인");
        return "pages/authentication/split/login";
    }

    @GetMapping("/pages/authentication/split/logout")
    public String pagesAuthSplitLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return performLogout(request, redirectAttributes);
    }

    private String performLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        sessionAuthService.logout(request.getSession(false));
        redirectAttributes.addFlashAttribute("logoutSuccess", true);
        return "redirect:/pages/authentication/simple/login";
    }

    @GetMapping("/pages/authentication/split/register")
    public String pagesAuthSplitRegister(Model model) {
        model.addAttribute("title", "회원가입");
        return "pages/authentication/split/register";
    }

    @GetMapping("/pages/authentication/split/forgot-password")
    public String pagesAuthSplitForgotPassword(Model model) {
        model.addAttribute("title", "비밀번호 찾기");
        return "pages/authentication/split/forgot-password";
    }

    @GetMapping("/pages/authentication/split/confirm-mail")
    public String pagesAuthSplitConfirmMail(Model model) {
        model.addAttribute("title", "메일 확인");
        return "pages/authentication/split/confirm-mail";
    }

    @GetMapping("/pages/authentication/split/reset-password")
    public String pagesAuthSplitResetPassword(Model model) {
        model.addAttribute("title", "비밀번호 재설정");
        return "pages/authentication/split/reset-password";
    }

    @GetMapping("/pages/authentication/split/lock-screen")
    public String pagesAuthSplitLockScreen(Model model) {
        model.addAttribute("title", "화면 잠금");
        return "pages/authentication/split/lock-screen";
    }

    // Pages - Authentication - Wizard
    @GetMapping("/pages/authentication/wizard")
    public String pagesAuthWizard(Model model) {
        model.addAttribute("title", "마법사");
        return "pages/authentication/wizard";
    }

    // Pages - User
    @GetMapping("/pages/user/profile")
    public String pagesUserProfile(Model model) {
        model.addAttribute("title", "프로필");
        return "pages/user/profile";
    }

    @GetMapping("/pages/user/settings")
    public String pagesUserSettings(Model model) {
        model.addAttribute("title", "설정");
        return "pages/user/settings";
    }

    // Pages - Pricing
    @GetMapping("/pages/pricing/pricing-default")
    public String pagesPricingDefault(Model model) {
        model.addAttribute("title", "기본 요금제");
        return "pages/pricing/pricing-default";
    }

    @GetMapping("/pages/pricing/pricing-alt")
    public String pagesPricingAlt(Model model) {
        model.addAttribute("title", "대체 요금제");
        return "pages/pricing/pricing-alt";
    }

    // Pages - FAQ
    @GetMapping("/pages/faq/faq-basic")
    public String pagesFaqBasic(Model model) {
        model.addAttribute("title", "FAQ 기본");
        return "pages/faq/faq-basic";
    }

    @GetMapping("/pages/faq/faq-alt")
    public String pagesFaqAlt(Model model) {
        model.addAttribute("title", "FAQ 대체");
        return "pages/faq/faq-alt";
    }

    @GetMapping("/pages/faq/faq-accordion")
    public String pagesFaqAccordion(Model model) {
        model.addAttribute("title", "FAQ 아코디언");
        return "pages/faq/faq-accordion";
    }

    // Pages - Errors
    @GetMapping("/pages/errors/404")
    public String pagesErrors404(Model model) {
        model.addAttribute("title", "404 - 페이지를 찾을 수 없음");
        return "pages/errors/404";
    }

    @GetMapping("/pages/errors/500")
    public String pagesErrors500(Model model) {
        model.addAttribute("title", "500 - 서버 내부 오류");
        return "pages/errors/500";
    }

    // Pages - Miscellaneous
    @GetMapping("/pages/miscellaneous/associations")
    public String pagesMiscAssociations(Model model) {
        model.addAttribute("title", "연동");
        return "pages/miscellaneous/associations";
    }

    @GetMapping("/pages/miscellaneous/invite-people")
    public String pagesMiscInvitePeople(Model model) {
        model.addAttribute("title", "사용자 초대");
        return "pages/miscellaneous/invite-people";
    }

    @GetMapping("/pages/miscellaneous/privacy-policy")
    public String pagesMiscPrivacyPolicy(Model model) {
        model.addAttribute("title", "개인정보 처리방침");
        return "pages/miscellaneous/privacy-policy";
    }
}
