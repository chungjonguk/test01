package com.example.springbootapp.controller.page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * 화면 경로: {@code /documentation/*}
 * <p>템플릿 사용법·커스터마이징·FAQ 등 문서화 데모 화면을 렌더링합니다.</p>
 */
@Controller
public class DocumentationController {
	/**
	 * @return out: Thymeleaf view path {@code documentation/getting-started}
	 */
	@GetMapping("/documentation/getting-started")
	public String documentationGettingStarted(Model model) {
		model.addAttribute("title", "시작하기");
		return "documentation/getting-started";
	}
	/**
	 * @return out: Thymeleaf view path {@code documentation/customization/configuration}
	 */
	@GetMapping("/documentation/customization/configuration")
	public String documentationCustomizationConfiguration(Model model) {
		model.addAttribute("title", "설정");
		return "documentation/customization/configuration";
	}
	/**
	 * @return out: Thymeleaf view path {@code documentation/customization/styling}
	 */
	@GetMapping("/documentation/customization/styling")
	public String documentationCustomizationStyling(Model model) {
		model.addAttribute("title", "스타일");
		return "documentation/customization/styling";
	}
	/**
	 * @return out: Thymeleaf view path {@code documentation/customization/dark-mode}
	 */
	@GetMapping("/documentation/customization/dark-mode")
	public String documentationCustomizationDarkMode(Model model) {
		model.addAttribute("title", "다크 모드");
		return "documentation/customization/dark-mode";
	}
	/**
	 * @return out: Thymeleaf view path {@code documentation/customization/plugin}
	 */
	@GetMapping("/documentation/customization/plugin")
	public String documentationCustomizationPlugin(Model model) {
		model.addAttribute("title", "플러그인");
		return "documentation/customization/plugin";
	}
	/**
	 * @return out: Thymeleaf view path {@code documentation/faq}
	 */
	@GetMapping("/documentation/faq")
	public String documentationFaq(Model model) {
		model.addAttribute("title", "FAQ");
		return "documentation/faq";
	}
	/**
	 * @return out: Thymeleaf view path {@code documentation/gulp}
	 */
	@GetMapping("/documentation/gulp")
	public String documentationGulp(Model model) {
		model.addAttribute("title", "Gulp");
		return "documentation/gulp";
	}
	/**
	 * @return out: Thymeleaf view path {@code documentation/design-file}
	 */
	@GetMapping("/documentation/design-file")
	public String documentationDesignFile(Model model) {
		model.addAttribute("title", "디자인 파일");
		return "documentation/design-file";
	}
}
