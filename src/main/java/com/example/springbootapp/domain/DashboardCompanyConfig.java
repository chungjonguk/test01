package com.example.springbootapp.domain;

import java.time.LocalDateTime;

/**
 * 업체별 대시보드 위젯 표시 설정.
 */
public class DashboardCompanyConfig {

	private Long companyId;
	private String hiddenJson;
	private String regId;
	private LocalDateTime regDt;
	private String updateId;
	private LocalDateTime updateDt;

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public String getHiddenJson() {
		return hiddenJson;
	}

	public void setHiddenJson(String hiddenJson) {
		this.hiddenJson = hiddenJson;
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
}
