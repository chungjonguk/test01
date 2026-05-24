package com.example.springbootapp.dto;
/**
 * 캘린더 일정 등록·수정 폼/API 전달 객체.
 * <p>in/out: 일정 저장·조회 API request/response body</p>
 * <ul>
 *   <li>{@code eventId} — in/out: 일정 ID (수정 시)</li>
 *   <li>{@code title} — in/out: 제목</li>
 *   <li>{@code categoryCd} — in/out: 카테고리 코드</li>
 *   <li>{@code labelCd} — in/out: 라벨 코드</li>
 *   <li>{@code startDt} — in/out: 시작 일시</li>
 *   <li>{@code endDt} — in/out: 종료 일시</li>
 *   <li>{@code location} — in/out: 장소</li>
 *   <li>{@code description} — in/out: 설명</li>
 *   <li>{@code allDay} — in/out: 종일 여부</li>
 * </ul>
 */
public class CalendarEventFormDto {
	private Long eventId;
	private String title;
	private String categoryCd;
	private String labelCd;
	private String startDt;
	private String endDt;
	private String location;
	private String description;
	private Boolean allDay;
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
	public String getStartDt() {
		return startDt;
	}
	public void setStartDt(String startDt) {
		this.startDt = startDt;
	}
	public String getEndDt() {
		return endDt;
	}
	public void setEndDt(String endDt) {
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
	public Boolean getAllDay() {
		return allDay;
	}
	public void setAllDay(Boolean allDay) {
		this.allDay = allDay;
	}
	@Override
	public String toString() {
		return "CalendarEventFormDto{eventId=" + eventId + ", title='" + title + "', categoryCd='" + categoryCd
				+ "', labelCd='" + labelCd + "', startDt='" + startDt + "', endDt='" + endDt + "', location='"
				+ location + "', description='" + description + "', allDay=" + allDay + "}";
	}
}
