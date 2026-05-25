package com.example.springbootapp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.springbootapp.config.NasStorageProperties;
import com.example.springbootapp.storage.NasMediaType;
/**
 * NAS 저장소 관리 화면.
 * <ul>
 *   <li>{@code /admin/nas} — 설정·용량</li>
 *   <li>{@code /admin/media-storage} — 파일 업로드·목록</li>
 * </ul>
 */
@Controller
@RequestMapping("/admin")
public class AdminNasController {
	private final NasStorageProperties nasStorage;
	public AdminNasController(NasStorageProperties nasStorage) {
		this.nasStorage = nasStorage;
	}
	/**
	 * NAS 설정·용량 요약 화면.
	 *
	 * @return out: {@code admin/nas-settings}
	 */
	@GetMapping({"/nas", "/nas-settings", "/nas-settings.html"})
	public String nasSettings(Model model) {
		model.addAttribute("title", "NAS 설정");
		model.addAttribute("nasAdminTab", "settings");
		populateNasConfig(model);
		return "admin/nas-settings";
	}
	/**
	 * NAS 파일 업로드·목록 화면.
	 *
	 * @return out: {@code admin/media-storage}
	 */
	@GetMapping({"/media-storage", "/media-storage.html"})
	public String mediaStorage(Model model) {
		model.addAttribute("title", "NAS 파일 관리");
		model.addAttribute("nasAdminTab", "files");
		populateNasConfig(model);
		return "admin/media-storage";
	}
	private void populateNasConfig(Model model) {
		model.addAttribute("nasEnabled", nasStorage.isEnabled());
		model.addAttribute("nasBasePath", nasStorage.getBasePath());
		model.addAttribute("nasUploadSubdir", nasStorage.getUploadSubdir());
		model.addAttribute("nasUrlPrefix", nasStorage.normalizedUrlPrefix());
		model.addAttribute("nasPathImages", nasStorage.resolveMediaDir(NasMediaType.IMAGE).toString());
		model.addAttribute("nasPathDocuments", nasStorage.resolveMediaDir(NasMediaType.DOCUMENT).toString());
		model.addAttribute("nasPathVideos", nasStorage.resolveMediaDir(NasMediaType.VIDEO).toString());
		model.addAttribute("nasPathProducts", nasStorage.resolveMediaDir(NasMediaType.PRODUCT).toString());
	}
}
