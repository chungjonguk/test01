package com.example.springbootapp.controller;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.springbootapp.auth.LoginSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.springbootapp.dto.UserRegisterDto;
import com.example.springbootapp.service.UserService;
/**
 * 사용자 관리 API 및 페이지 컨트롤러.
 * <p>기본 경로: {@code /users}</p>
 */
@Controller
@RequestMapping("/users")
public class UserController {
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private final UserService userService;
	public UserController(UserService userService) {
		this.userService = userService;
	}
	/**
	 * 아이디 중복 여부를 확인합니다.
	 *
	 * @param id in: 확인할 사용자 아이디
	 * @return out: JSON {@code Map} — {@code available}, {@code message}
	 */
	@GetMapping("/check-id")
	@ResponseBody
	public Map<String, Object> checkId(@RequestParam(required = false) String id) {
		String trimmedId = trim(id);
		if (trimmedId.isEmpty()) {
			return Map.of(
					"available", false,
					"message", "아이디를 입력해 주세요.");
		}
		if (userService.existsById(trimmedId)) {
			return Map.of(
					"available", false,
					"message", "이미 등록된 아이디입니다.");
		}
		return Map.of(
				"available", true,
				"message", "사용 가능한 아이디입니다.");
	}
	/**
	 * 이메일 중복 및 형식 유효성을 확인합니다.
	 *
	 * @param email in: 확인할 이메일 주소
	 * @return out: JSON {@code Map} — {@code available}, {@code message}
	 */
	@GetMapping("/check-email")
	@ResponseBody
	public Map<String, Object> checkEmail(@RequestParam(required = false) String email) {
		String trimmedEmail = trim(email);
		if (trimmedEmail.isEmpty()) {
			return Map.of(
					"available", false,
					"message", "이메일을 입력해 주세요.");
		}
		if (!isValidEmail(trimmedEmail)) {
			return Map.of(
					"available", false,
					"message", "올바른 이메일 형식이 아닙니다.");
		}
		if (userService.existsByEmail(trimmedEmail)) {
			return Map.of(
					"available", false,
					"message", "이미 등록된 이메일입니다.");
		}
		return Map.of(
				"available", true,
				"message", "사용 가능한 이메일입니다.");
	}
	/**
	 * 사용자 관리 페이지를 렌더링합니다.
	 *
	 * @param model in: 뷰에 전달할 모델
	 * @return out: 뷰 이름 {@code users}
	 */
	@GetMapping
	public String usersPage(Model model, @RequestParam(required = false) String email) {
		model.addAttribute("title", "사용자 관리");
		model.addAttribute("users", userService.findAll());
		UserRegisterDto userForm = new UserRegisterDto();
		if (email != null && !email.isBlank()) {
			userForm.setEmail(email.trim());
		}
		model.addAttribute("userForm", userForm);
		return "users";
	}
	/**
	 * 새 사용자를 등록합니다.
	 *
	 * @param userForm           in: 사용자 등록 폼 데이터
	 * @param redirectAttributes in: 리다이렉트 시 플래시 메시지 전달용
	 * @return out: {@code redirect:/users}
	 */
	@PostMapping
	public String createUser(@ModelAttribute("userForm") UserRegisterDto userForm, RedirectAttributes redirectAttributes) {
		try {
			userService.register(userForm);
			redirectAttributes.addFlashAttribute("successMessage", "사용자가 등록되었습니다: " + trim(userForm.getId()));
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/users";
	}
	/**
	 * 사용자 비밀번호를 아이디와 동일한 값으로 초기화합니다.
	 *
	 * @param id                 in: 대상 사용자 아이디
	 * @param loginUser          in: 로그인 세션 (수정자 기록용, 없으면 대상 아이디)
	 * @param redirectAttributes in: 리다이렉트 시 플래시 메시지 전달용
	 * @return out: {@code redirect:/users}
	 */
	@PostMapping("/{id}/reset-password")
	public String resetPassword(
			@PathVariable String id,
			@ModelAttribute("loginUser") LoginSession loginUser,
			RedirectAttributes redirectAttributes) {
		try {
			String updateId = loginUser != null ? trim(loginUser.getUserId()) : "";
			userService.resetPasswordToInitial(id, updateId);
			redirectAttributes.addFlashAttribute("successMessage",
					"비밀번호가 아이디(" + trim(id) + ")와 동일하게 초기화되었습니다.");
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/users";
	}
	private String trim(String value) {
		return value == null ? "" : value.trim();
	}
	private boolean isValidEmail(String email) {
		return EMAIL_PATTERN.matcher(email).matches();
	}
}
