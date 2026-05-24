package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class ScreenTableMap {
	private String uriPath;
	private String screenId;
	private String primaryTable;
	private String relatedTables;
	private String dataType;
	private String tableDesc;
	private String regId;
	private LocalDateTime regDt;
	private String updateId;
	private LocalDateTime updateDt;
	public String getUriPath() {
		return uriPath;
	}
	public void setUriPath(String uriPath) {
		this.uriPath = uriPath;
	}
	public String getScreenId() {
		return screenId;
	}
	public void setScreenId(String screenId) {
		this.screenId = screenId;
	}
	public String getPrimaryTable() {
		return primaryTable;
	}
	public void setPrimaryTable(String primaryTable) {
		this.primaryTable = primaryTable;
	}
	public String getRelatedTables() {
		return relatedTables;
	}
	public void setRelatedTables(String relatedTables) {
		this.relatedTables = relatedTables;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getTableDesc() {
		return tableDesc;
	}
	public void setTableDesc(String tableDesc) {
		this.tableDesc = tableDesc;
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
		return "ScreenTableMap{uriPath='" + uriPath + "', screenId='" + screenId + "', primaryTable='" + primaryTable
				+ "', relatedTables='" + relatedTables + "', dataType='" + dataType + "', tableDesc='" + tableDesc
				+ "', regId='" + regId + "', regDt=" + regDt + ", updateId='" + updateId + "', updateDt=" + updateDt
				+ "}";
	}
}
