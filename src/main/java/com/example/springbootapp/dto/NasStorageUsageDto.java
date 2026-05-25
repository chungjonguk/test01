package com.example.springbootapp.dto;
import java.util.List;
/**
 * NAS 폴더 사용량 요약 (Using Storage 위젯·API).
 */
public class NasStorageUsageDto {
	private String uploadRoot;
	private long quotaBytes;
	private long usedBytes;
	private long freeBytes;
	private List<CategoryUsage> categories;
	public String getUploadRoot() {
		return uploadRoot;
	}
	public void setUploadRoot(String uploadRoot) {
		this.uploadRoot = uploadRoot;
	}
	public long getQuotaBytes() {
		return quotaBytes;
	}
	public void setQuotaBytes(long quotaBytes) {
		this.quotaBytes = quotaBytes;
	}
	public long getUsedBytes() {
		return usedBytes;
	}
	public void setUsedBytes(long usedBytes) {
		this.usedBytes = usedBytes;
	}
	public long getFreeBytes() {
		return freeBytes;
	}
	public void setFreeBytes(long freeBytes) {
		this.freeBytes = freeBytes;
	}
	public List<CategoryUsage> getCategories() {
		return categories;
	}
	public void setCategories(List<CategoryUsage> categories) {
		this.categories = categories;
	}
	public static class CategoryUsage {
		private String code;
		private String label;
		private String folder;
		private long bytes;
		private double percentOfQuota;
		private String barClass;
		private String dotClass;
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getFolder() {
			return folder;
		}
		public void setFolder(String folder) {
			this.folder = folder;
		}
		public long getBytes() {
			return bytes;
		}
		public void setBytes(long bytes) {
			this.bytes = bytes;
		}
		public double getPercentOfQuota() {
			return percentOfQuota;
		}
		public void setPercentOfQuota(double percentOfQuota) {
			this.percentOfQuota = percentOfQuota;
		}
		public String getBarClass() {
			return barClass;
		}
		public void setBarClass(String barClass) {
			this.barClass = barClass;
		}
		public String getDotClass() {
			return dotClass;
		}
		public void setDotClass(String dotClass) {
			this.dotClass = dotClass;
		}
	}
}
