package com.example.springbootapp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.config.web.PublicPathCryptoService;

/**
 * 클라이언트용 공개(암호화) URL 변환 API.
 */
@RestController
@Profile("!test")
@RequestMapping("/api/url")
public class PublicPathApiController {

	private final PublicPathCryptoService publicPathCryptoService;

	public PublicPathApiController(PublicPathCryptoService publicPathCryptoService) {
		this.publicPathCryptoService = publicPathCryptoService;
	}

	@GetMapping("/public")
	public Map<String, String> encode(@RequestParam String path) {
		Map<String, String> body = new LinkedHashMap<>();
		body.put("path", publicPathCryptoService.toPublicPath(path));
		body.put("logical", publicPathCryptoService.toLogicalPath(path));
		return body;
	}

	@PostMapping("/public/batch")
	public Map<String, String> encodeBatch(@RequestBody List<String> paths) {
		Map<String, String> out = new LinkedHashMap<>();
		if (paths == null) {
			return out;
		}
		for (String path : paths) {
			if (path != null && !path.isBlank()) {
				out.put(path, publicPathCryptoService.toPublicPath(path));
			}
		}
		return out;
	}
}
