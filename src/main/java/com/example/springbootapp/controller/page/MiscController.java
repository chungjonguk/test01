package com.example.springbootapp.controller.page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * 화면 경로: {@code /changelog}, {@code /widgets}
 * <p>변경 이력·위젯 데모 등 기타 단일 페이지를 렌더링합니다.</p>
 */
@Controller
public class MiscController {
	/**
	 * @return out: Thymeleaf view path {@code changelog}
	 */
	@GetMapping("/changelog")
	public String changelog(Model model) {
		model.addAttribute("title", "변경 이력");
		return "changelog";
	}
	/**
	 * @return out: Thymeleaf view path {@code widgets}
	 */
	@GetMapping("/widgets")
	public String widgets(Model model) {
		model.addAttribute("title", "위젯");
		return "widgets";
	}
}
