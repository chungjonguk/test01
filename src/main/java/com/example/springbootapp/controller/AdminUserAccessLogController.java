package com.example.springbootapp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * 화면 경로: {@code /admin/user-access-logs}
 * <p>사용자 접속·로그인 이력 조회 화면을 렌더링합니다.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminUserAccessLogController {
	/**
	 * @return out: Thymeleaf view path {@code admin/user-access-logs}
	 */
	@GetMapping({"/user-access-logs", "/user-access-logs.html"})
	public String userAccessLogs() {
		return "admin/user-access-logs";
	}
}
