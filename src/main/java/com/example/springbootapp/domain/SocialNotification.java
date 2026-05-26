package com.example.springbootapp.domain;

import java.time.LocalDateTime;

public class SocialNotification {

	private Long notificationId;
	private String userNm;
	private String senderNm;
	private String message;
	private String sectionCd;
	private String timeIcon;
	private String readYn;
	private LocalDateTime notifiedDt;

	public Long getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

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

	public String getReadYn() {
		return readYn;
	}

	public void setReadYn(String readYn) {
		this.readYn = readYn;
	}

	public LocalDateTime getNotifiedDt() {
		return notifiedDt;
	}

	public void setNotifiedDt(LocalDateTime notifiedDt) {
		this.notifiedDt = notifiedDt;
	}
}
