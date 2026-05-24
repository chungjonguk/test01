package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class CalendarEvent {
	private Long eventId;
	private String title;
	private String categoryCd;
	private String labelCd;
	private LocalDateTime startDt;
	private LocalDateTime endDt;
	private String location;
	private String description;
	private String allDayYn;
	private String regId;
	private LocalDateTime regDt;
	private String updateId;
	private LocalDateTime updateDt;
	public Long getEventId() {
		return eventId;
	}
	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCategoryCd() {
		return categoryCd;
	}
	public void setCategoryCd(String categoryCd) {
		this.categoryCd = categoryCd;
	}
	public String getLabelCd() {
		return labelCd;
	}
	public void setLabelCd(String labelCd) {
		this.labelCd = labelCd;
	}
	public LocalDateTime getStartDt() {
		return startDt;
	}
	public void setStartDt(LocalDateTime startDt) {
		this.startDt = startDt;
	}
	public LocalDateTime getEndDt() {
		return endDt;
	}
	public void setEndDt(LocalDateTime endDt) {
		this.endDt = endDt;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAllDayYn() {
		return allDayYn;
	}
	public void setAllDayYn(String allDayYn) {
		this.allDayYn = allDayYn;
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
		return "CalendarEvent{eventId=" + eventId + ", title='" + title + "', categoryCd='" + categoryCd
				+ "', labelCd='" + labelCd + "', startDt=" + startDt + ", endDt=" + endDt + ", location='" + location
				+ "', description='" + description + "', allDayYn='" + allDayYn + "', regId='" + regId + "', regDt="
				+ regDt + ", updateId='" + updateId + "', updateDt=" + updateDt + "}";
	}
}
