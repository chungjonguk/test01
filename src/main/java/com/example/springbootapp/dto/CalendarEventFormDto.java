package com.example.springbootapp.dto;

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
}
