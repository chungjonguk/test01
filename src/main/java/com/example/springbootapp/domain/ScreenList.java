package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class ScreenList {
	private String screenId;
	private String screenNm;
	private String uriPath;
	private String templatePath;
	private int sortOrd;
	private String useYn;
	/** 메뉴 섹션 그룹 (대시보드/쇼핑몰/앱/페이지/관리자 등) */
	private String menuGroup;
	/** Font Awesome 등 아이콘 클래스 */
	private String iconClass;
	/** 상위 화면 ID (선택) */
	private String parentScreenId;
	private String regId;
	private LocalDateTime regDt;
	private String updateId;
	private LocalDateTime updateDt;
	public String getScreenId() {
		return screenId;
	}
	public void setScreenId(String screenId) {
		this.screenId = screenId;
	}
	public String getScreenNm() {
		return screenNm;
	}
	public void setScreenNm(String screenNm) {
		this.screenNm = screenNm;
	}
	public String getUriPath() {
		return uriPath;
	}
	public void setUriPath(String uriPath) {
		this.uriPath = uriPath;
	}
	public String getTemplatePath() {
		return templatePath;
	}
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}
	public int getSortOrd() {
		return sortOrd;
	}
	public void setSortOrd(int sortOrd) {
		this.sortOrd = sortOrd;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	public String getMenuGroup() {
		return menuGroup;
	}
	public void setMenuGroup(String menuGroup) {
		this.menuGroup = menuGroup;
	}
	public String getIconClass() {
		return iconClass;
	}
	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}
	public String getParentScreenId() {
		return parentScreenId;
	}
	public void setParentScreenId(String parentScreenId) {
		this.parentScreenId = parentScreenId;
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
	public String getUpdateId() {
		return updateId;
	}
	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}
	public LocalDateTime getUpdateDt() {
		return updateDt;
	}
	public void setUpdateDt(LocalDateTime updateDt) {
		this.updateDt = updateDt;
	}
	@Override
	public String toString() {
		return "ScreenList{screenId='" + screenId + "', screenNm='" + screenNm + "', uriPath='" + uriPath
				+ "', templatePath='" + templatePath + "', sortOrd=" + sortOrd + ", useYn='" + useYn + "', regId='"
				+ regId + "', regDt=" + regDt + ", updateId='" + updateId + "', updateDt=" + updateDt + "}";
	}
}
