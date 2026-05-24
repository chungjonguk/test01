package com.example.springbootapp.web;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.springbootapp.dto.CodeOption;
import com.example.springbootapp.service.CommonCodeService;
/**
 * MVC 페이지에 공통코드 콤보 옵션 맵을 주입합니다.
 */
@Profile("!test")
@ControllerAdvice(basePackages = "com.example.springbootapp.controller")
public class PageCodeOptionsAdvice {
	private final CommonCodeService commonCodeService;
	public PageCodeOptionsAdvice(CommonCodeService commonCodeService) {
		this.commonCodeService = commonCodeService;
	}
	@ModelAttribute("codeOptions")
	public Map<String, List<CodeOption>> codeOptions() {
		return commonCodeService.findAllActiveOptionsMap();
	}
	/** Thymeleaf inline JSON (code-select-loader.js) */
	@ModelAttribute("codeOptionsForJs")
	public Map<String, List<Map<String, String>>> codeOptionsForJs() {
		Map<String, List<CodeOption>> src = commonCodeService.findAllActiveOptionsMap();
		Map<String, List<Map<String, String>>> out = new LinkedHashMap<>();
		for (Map.Entry<String, List<CodeOption>> entry : src.entrySet()) {
			List<Map<String, String>> rows = new ArrayList<>();
			for (CodeOption opt : entry.getValue()) {
				Map<String, String> row = new LinkedHashMap<>();
				row.put("value", opt.getValue() != null ? opt.getValue() : "");
				row.put("label", opt.getLabel() != null ? opt.getLabel() : "");
				rows.add(row);
			}
			out.put(entry.getKey(), rows);
		}
		return out;
	}
}
