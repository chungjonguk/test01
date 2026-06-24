package com.example.springbootapp.config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.example.springbootapp.storage.NasMediaType;
@Component
@ConfigurationProperties(prefix = "app.storage.nas")
public class NasStorageProperties {
	private boolean enabled = true;
	private String basePath = "E:/nas-storage/printmall";
	private String uploadSubdir = "uploads";
	private String urlPrefix = "/uploads";
	/** Using Storage 위젯 표시용 할당 용량(GB) */
	private int quotaGb = 2;
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getBasePath() {
		return basePath;
	}
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	public String getUploadSubdir() {
		return uploadSubdir;
	}
	public void setUploadSubdir(String uploadSubdir) {
		this.uploadSubdir = uploadSubdir;
	}
	public String getUrlPrefix() {
		return urlPrefix;
	}
	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
	public int getQuotaGb() {
		return quotaGb;
	}
	public void setQuotaGb(int quotaGb) {
		this.quotaGb = quotaGb;
	}
	public long getQuotaBytes() {
		int gb = quotaGb < 1 ? 1 : quotaGb;
		return (long) gb * 1024L * 1024L * 1024L;
	}
	public Path resolveUploadRoot() {
		if (enabled) {
			return Paths.get(basePath, uploadSubdir.split("/")).toAbsolutePath().normalize();
		}
		return Paths.get("src", "main", "resources", "static", "uploads").toAbsolutePath().normalize();
	}
	public Path resolveMediaDir(NasMediaType type) {
		return resolveUploadRoot().resolve(type.getFolderName()).normalize();
	}
	public String normalizedUrlPrefix() {
		String prefix = urlPrefix == null || urlPrefix.isBlank() ? "/uploads" : urlPrefix.trim();
		if (!prefix.startsWith("/")) {
			prefix = "/" + prefix;
		}
		return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
	}
	public String buildPublicUrl(NasMediaType type, String filename) {
		return normalizedUrlPrefix() + "/" + type.getFolderName() + "/" + filename;
	}
	public void ensureAllMediaDirsExist() throws IOException {
		for (NasMediaType type : NasMediaType.values()) {
			Files.createDirectories(resolveMediaDir(type));
		}
	}
	/** @deprecated ProductImageStorageService 호환 */
	@Deprecated
	public Path resolveProductUploadDir() {
		return resolveMediaDir(NasMediaType.PRODUCT);
	}
	/** @deprecated ProductImageStorageService 호환 */
	@Deprecated
	public void ensureProductUploadDirExists() throws IOException {
		Files.createDirectories(resolveProductUploadDir());
	}
}
