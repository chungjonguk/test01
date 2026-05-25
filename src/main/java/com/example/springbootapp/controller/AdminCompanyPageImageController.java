package com.example.springbootapp.controller;

import com.example.springbootapp.config.CompanyPageImageCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 업체별 페이지 이미지 등록 화면 — {@code /admin/company-page-images}
 */
@Controller
@RequestMapping("/admin")
public class AdminCompanyPageImageController {

	@GetMapping({"/company-page-images", "/company-page-images.html"})
	public String companyPageImages(Model model) {
		model.addAttribute("companyPageImageSlots", CompanyPageImageCatalog.toMaps());
		return "admin/company-page-images";
	}
}
