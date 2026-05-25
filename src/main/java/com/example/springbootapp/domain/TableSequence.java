package com.example.springbootapp.domain;

import java.time.LocalDateTime;

/**
 * {@code sys_table_sequence} — 테이블별 PK 채번 정의.
 */
public class TableSequence {

	private String seqName;
	private String tableName;
	private String columnName;
	private long nextVal;
	private int incrementBy;
	private long minVal;
	private Long maxVal;
	private String description;
	private String useYn;
	private String regId;
	private LocalDateTime regDt;
	private String updateId;
	private LocalDateTime updateDt;

	public String getSeqName() {
		return seqName;
	}

	public void setSeqName(String seqName) {
		this.seqName = seqName;
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

	public long getNextVal() {
		return nextVal;
	}

	public void setNextVal(long nextVal) {
		this.nextVal = nextVal;
	}

	public int getIncrementBy() {
		return incrementBy;
	}

	public void setIncrementBy(int incrementBy) {
		this.incrementBy = incrementBy;
	}

	public long getMinVal() {
		return minVal;
	}

	public void setMinVal(long minVal) {
		this.minVal = minVal;
	}

	public Long getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(Long maxVal) {
		this.maxVal = maxVal;
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
}
