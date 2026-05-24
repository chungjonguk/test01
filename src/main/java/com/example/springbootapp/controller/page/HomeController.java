package com.example.springbootapp.controller.page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * 화면 경로: {@code /}, {@code /dashboard}
 * <p>애플리케이션 홈·기본 대시보드 진입 화면을 렌더링합니다.</p>
 */
@Controller
public class HomeController {
	@Value("${app.brand-name:PrintMall}")
	private String appBrandName;
	/**
	 * @return out: Thymeleaf view path {@code index}
	 */
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", appBrandName + " | 대시보드 및 웹 앱 템플릿");
		return "index";
	}
	/**
	 * @return out: Thymeleaf view path {@code index}
	 */
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("title", "대시보드 - 기본");
		return "index";
	}
}
