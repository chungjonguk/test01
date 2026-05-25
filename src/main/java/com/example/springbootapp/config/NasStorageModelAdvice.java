package com.example.springbootapp.config;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.springbootapp.storage.NasMediaType;
/**
 * NAS 저장 경로를 Thymeleaf Model에 노출합니다.
 */
@Profile("!test")
@ControllerAdvice(basePackages = "com.example.springbootapp.controller")
public class NasStorageModelAdvice {
	private final NasStorageProperties nasStorage;
	public NasStorageModelAdvice(NasStorageProperties nasStorage) {
		this.nasStorage = nasStorage;
	}
	@ModelAttribute("nasUploadRoot")
	public String nasUploadRoot() {
		return nasStorage.resolveUploadRoot().toString();
	}
	@ModelAttribute("nasQuotaGb")
	public int nasQuotaGb() {
		return nasStorage.getQuotaGb();
	}
	@ModelAttribute
	public void nasFolderPaths(Model model) {
		model.addAttribute("nasPathImages", nasStorage.resolveMediaDir(NasMediaType.IMAGE).toString());
		model.addAttribute("nasPathDocuments", nasStorage.resolveMediaDir(NasMediaType.DOCUMENT).toString());
		model.addAttribute("nasPathVideos", nasStorage.resolveMediaDir(NasMediaType.VIDEO).toString());
		model.addAttribute("nasPathProducts", nasStorage.resolveMediaDir(NasMediaType.PRODUCT).toString());
	}
}
