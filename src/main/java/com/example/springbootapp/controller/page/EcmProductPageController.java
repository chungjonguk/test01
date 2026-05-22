package com.example.springbootapp.controller.page;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.springbootapp.domain.EcmProduct;
import com.example.springbootapp.dto.EcmProductFormDto;
import com.example.springbootapp.service.EcmProductService;

@Controller
public class EcmProductPageController {

	private final EcmProductService ecmProductService;

	public EcmProductPageController(EcmProductService ecmProductService) {
		this.ecmProductService = ecmProductService;
	}

	@GetMapping({"/app/e-commerce/product/product-manage", "/app/e-commerce/product/product-manage.html"})
	public String productManage(Model model) {
		model.addAttribute("title", "상품 관리");
		return "app/e-commerce/product/product-manage";
	}

	@GetMapping({"/app/e-commerce/product/product-register", "/app/e-commerce/product/product-register.html"})
	public String productRegister(@RequestParam(required = false) Long id, Model model) {
		EcmProductFormDto form = new EcmProductFormDto();
		boolean editMode = false;
		if (id != null) {
			EcmProduct product = ecmProductService.findById(id);
			if (product != null) {
				editMode = true;
				form.setProductId(product.getProductId());
				form.setProductNm(product.getProductNm());
				form.setCategoryCd(product.getCategoryCd());
				form.setPrice(product.getPrice());
				form.setStockQty(product.getStockQty());
				form.setStatusCd(product.getStatusCd());
				form.setDescription(product.getDescription());
				String mainUrl = product.getImgUrl() != null && !product.getImgUrl().isBlank()
						? EcmProductService.resolveDisplayPath(product.getImgUrl())
						: null;
				List<String> urls = new ArrayList<>(ecmProductService.findImageUrls(id));
				form.setImageUrls(urls);
				form.setMainImageUrl(mainUrl);
				form.setImgUrl(mainUrl != null ? mainUrl : (urls.isEmpty() ? null : urls.get(0)));
			}
		}
		if (!editMode) {
			form.setStatusCd("ACTIVE");
			form.setStockQty(0);
		}
		model.addAttribute("title", editMode ? "상품 수정" : "상품 등록");
		model.addAttribute("editMode", editMode);
		model.addAttribute("productForm", form);
		model.addAttribute("maxImages", EcmProductFormDto.MAX_IMAGES);
		model.addAttribute("initialImageUrls", form.getImageUrls());
		model.addAttribute("initialMainImageUrl", form.getMainImageUrl());
		if (editMode && form.getProductId() != null) {
			model.addAttribute("detailPreviewUrl",
					"/app/e-commerce/product/product-details?id=" + form.getProductId());
			model.addAttribute("imagesPreviewUrl",
					"/app/e-commerce/product/product-images?id=" + form.getProductId());
		}
		return "app/e-commerce/product/product-register";
	}

	@GetMapping({"/app/e-commerce/product/product-images", "/app/e-commerce/product/product-images.html"})
	public String productImages(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("title", "상품 이미지");
		if (id == null) {
			model.addAttribute("missingId", true);
			return "app/e-commerce/product/product-images";
		}
		EcmProduct product = ecmProductService.findById(id);
		if (product == null) {
			model.addAttribute("productNotFound", true);
			model.addAttribute("requestedProductId", id);
			return "app/e-commerce/product/product-images";
		}
		String mainUrl = product.getImgUrl() != null && !product.getImgUrl().isBlank()
				? EcmProductService.resolveDisplayPath(product.getImgUrl())
				: null;
		List<String> displayUrls = new ArrayList<>(ecmProductService.findImageUrls(id));
		model.addAttribute("product", product);
		model.addAttribute("displayImageUrls", displayUrls);
		model.addAttribute("mainImageUrl", mainUrl);
		model.addAttribute("editUrl", "/app/e-commerce/product/product-register?id=" + product.getProductId());
		model.addAttribute("detailUrl", "/app/e-commerce/product/product-details?id=" + product.getProductId());
		return "app/e-commerce/product/product-images";
	}

	@GetMapping({"/app/e-commerce/product/product-details", "/app/e-commerce/product/product-details.html"})
	public String productDetails(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("title", "상품 상세");
		if (id == null) {
			model.addAttribute("demoMode", true);
			return "app/e-commerce/product/product-details";
		}
		EcmProduct product = ecmProductService.findById(id);
		if (product == null) {
			model.addAttribute("productNotFound", true);
			model.addAttribute("requestedProductId", id);
			return "app/e-commerce/product/product-details";
		}
		List<String> displayUrls = new ArrayList<>(ecmProductService.findImageUrls(id));
		String mainDisplay = product.getImgUrl() != null && !product.getImgUrl().isBlank()
				? EcmProductService.resolveDisplayPath(product.getImgUrl())
				: (displayUrls.isEmpty() ? "/assets/img/products/1.jpg" : displayUrls.get(0));
		model.addAttribute("product", product);
		model.addAttribute("displayImageUrls", displayUrls);
		model.addAttribute("displayImgUrl", mainDisplay);
		model.addAttribute("imagesUrl", "/app/e-commerce/product/product-images?id=" + product.getProductId());
		model.addAttribute("editUrl", "/app/e-commerce/product/product-register?id=" + product.getProductId());
		return "app/e-commerce/product/product-details";
	}
}
