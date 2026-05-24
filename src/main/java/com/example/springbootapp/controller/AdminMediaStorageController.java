package com.example.springbootapp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * 화면 경로: {@code /admin/media-storage}
 * <p>NAS 미디어 파일 업로드·목록 조회 관리 화면을 렌더링합니다.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminMediaStorageController {
	/**
	 * @return out: Thymeleaf view path {@code admin/media-storage}
	 */
	@GetMapping({"/media-storage", "/media-storage.html"})
	public String mediaStorage() {
		return "admin/media-storage";
	}
}
