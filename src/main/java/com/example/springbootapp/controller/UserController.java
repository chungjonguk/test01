package com.example.springbootapp.controller;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.springbootapp.dto.UserRegisterDto;
import com.example.springbootapp.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

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

	@GetMapping
	public String usersPage(Model model) {
		model.addAttribute("title", "사용자 관리");
		model.addAttribute("users", userService.findAll());
		model.addAttribute("userForm", new UserRegisterDto());
		return "users";
	}

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

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private boolean isValidEmail(String email) {
		return EMAIL_PATTERN.matcher(email).matches();
	}
}
