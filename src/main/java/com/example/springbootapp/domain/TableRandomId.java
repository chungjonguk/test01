package com.example.springbootapp.domain;

import java.time.LocalDateTime;

/**
 * {@code sys_table_random_id} — 테이블별 난수 PK 채번 설정.
 */
public class TableRandomId {

	private String configName;
	private String tableName;
	private String columnName;
	private String idTypeCd;
	private long minVal;
	private long maxVal;
	private Integer stringLength;
	private int maxRetry;
	private String description;
	private String useYn;
	private String regId;
	private LocalDateTime regDt;
	private String updateId;
	private LocalDateTime updateDt;

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getIdTypeCd() {
		return idTypeCd;
	}

	public void setIdTypeCd(String idTypeCd) {
		this.idTypeCd = idTypeCd;
	}

	public long getMinVal() {
		return minVal;
	}

	public void setMinVal(long minVal) {
		this.minVal = minVal;
	}

	public long getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(long maxVal) {
		this.maxVal = maxVal;
	}

	public Integer getStringLength() {
		return stringLength;
	}

	public void setStringLength(Integer stringLength) {
		this.stringLength = stringLength;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public boolean isNumericType() {
		return idTypeCd == null || "N".equalsIgnoreCase(idTypeCd);
	}

	public boolean isStringType() {
		return "S".equalsIgnoreCase(idTypeCd);
	}
}
