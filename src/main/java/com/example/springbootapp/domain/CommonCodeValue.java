package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class CommonCodeValue {
	private String codeId;
	private String codeVal;
	private String useYn;
	private String regId;
	private LocalDateTime regdateDt;
	private String updateId;
	private LocalDateTime updateDt;
	public String getCodeId() {
		return codeId;
	}
	public void setCodeId(String codeId) {
		this.codeId = codeId;
	}
	public String getCodeVal() {
		return codeVal;
	}
	public void setCodeVal(String codeVal) {
		this.codeVal = codeVal;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	public String getRegId() {
		return regId;
	}
	public void setRegId(String regId) {
		this.regId = regId;
	}
	public LocalDateTime getRegdateDt() {
		return regdateDt;
	}
	public void setRegdateDt(LocalDateTime regdateDt) {
		this.regdateDt = regdateDt;
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
