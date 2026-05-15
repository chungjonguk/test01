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

import com.example.springbootapp.domain.User;
import com.example.springbootapp.service.UserService;
import com.example.springbootapp.util.PasswordPolicyValidator;

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
		model.addAttribute("userForm", new User());
		return "users";
	}

	@PostMapping
	public String createUser(@ModelAttribute("userForm") User userForm, RedirectAttributes redirectAttributes) {
		String id = trim(userForm.getId());
		String pw = userForm.getPw();
		String name = trim(userForm.getName());
		String sex = trim(userForm.getSex());
		String rrno = trim(userForm.getRrno());
		String email = trim(userForm.getEmail());
		String zipcode = trim(userForm.getZipcode());
		String address = trim(userForm.getAddress());
		String addressDetail = trim(userForm.getAddressDetail());

		if (id.isEmpty() || pw == null || pw.isBlank() || name.isEmpty() || sex.isEmpty() || rrno.isEmpty()
				|| email.isEmpty() || zipcode.isEmpty() || address.isEmpty() || addressDetail.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "모든 항목을 입력해 주세요.");
			return "redirect:/users";
		}
		if (!PasswordPolicyValidator.isValid(pw)) {
			redirectAttributes.addFlashAttribute("errorMessage", PasswordPolicyValidator.requirementMessage());
			return "redirect:/users";
		}
		if (!"남자".equals(sex) && !"여자".equals(sex)) {
			redirectAttributes.addFlashAttribute("errorMessage", "성별은 남자 또는 여자 중 하나를 선택해 주세요.");
			return "redirect:/users";
		}
		if (!isValidEmail(email)) {
			redirectAttributes.addFlashAttribute("errorMessage", "올바른 이메일 형식이 아닙니다.");
			return "redirect:/users";
		}
		if (userService.existsById(id)) {
			redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 아이디입니다: " + id);
			return "redirect:/users";
		}
		if (userService.existsByEmail(email)) {
			redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 이메일입니다: " + email);
			return "redirect:/users";
		}

		User user = new User();
		user.setId(id);
		user.setPw(pw);
		user.setName(name);
		user.setSex(sex);
		user.setRrno(rrno);
		user.setEmail(email);
		user.setZipcode(zipcode);
		user.setAddress(address);
		user.setAddressDetail(addressDetail);
		user.setUpdateId(id);
		userService.create(user);

		redirectAttributes.addFlashAttribute("successMessage", "사용자가 등록되었습니다: " + id);
		return "redirect:/users";
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private boolean isValidEmail(String email) {
		return EMAIL_PATTERN.matcher(email).matches();
	}
}
