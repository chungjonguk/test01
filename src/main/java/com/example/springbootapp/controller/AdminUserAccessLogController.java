package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminUserAccessLogController {

	@GetMapping({"/user-access-logs", "/user-access-logs.html"})
	public String userAccessLogs() {
		return "admin/user-access-logs";
	}
}
