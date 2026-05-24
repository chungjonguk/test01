package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class NasFile {
	private Long fileId;
	private String mediaTypeCd;
	private String storedNm;
	private String originalNm;
	private String fileExt;
	private Long fileSize;
	private String filePath;
	private String urlPath;
	private String contentType;
	private String regId;
	private LocalDateTime regDt;
	public Long getFileId() {
		return fileId;
	}
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	public String getMediaTypeCd() {
		return mediaTypeCd;
	}
	public void setMediaTypeCd(String mediaTypeCd) {
		this.mediaTypeCd = mediaTypeCd;
	}
	public String getStoredNm() {
		return storedNm;
	}
	public void setStoredNm(String storedNm) {
		this.storedNm = storedNm;
	}
	public String getOriginalNm() {
		return originalNm;
	}
	public void setOriginalNm(String originalNm) {
		this.originalNm = originalNm;
	}
	public String getFileExt() {
		return fileExt;
	}
	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getUrlPath() {
		return urlPath;
	}
	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getRegId() {
		return regId;
	}
	public void setRegId(String regId) {
		this.regId = regId;
	}
	public LocalDateTime getRegDt() {
		return regDt;
	}
	public void setRegDt(LocalDateTime regDt) {
		this.regDt = regDt;
	}
}
