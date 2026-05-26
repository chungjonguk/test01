package com.example.springbootapp.dto;

public class SocialNotificationFormDto {

	private String userNm;
	private String senderNm;
	private String message;
	private String sectionCd;
	private String timeIcon;

	public String getUserNm() {
		return userNm;
	}

	public void setUserNm(String userNm) {
		this.userNm = userNm;
	}

	public String getSenderNm() {
		return senderNm;
	}

	public void setSenderNm(String senderNm) {
		this.senderNm = senderNm;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSectionCd() {
		return sectionCd;
	}

	public void setSectionCd(String sectionCd) {
		this.sectionCd = sectionCd;
	}

	public String getTimeIcon() {
		return timeIcon;
	}

	public void setTimeIcon(String timeIcon) {
		this.timeIcon = timeIcon;
	}
}
