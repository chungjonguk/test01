package com.example.springbootapp.domain;

import java.time.LocalDateTime;

public class BizCompanyDomain {
	private Long domainId;
	private Long companyId;
	private String hostName;
	private String primaryYn;
	private String sslYn;
	private LocalDateTime sslCertNotBefore;
	private LocalDateTime sslCertNotAfter;
	private String sslCertSubject;
	private String sslCertIssuer;
	private Long sslCertFileId;
	private String verifyStatusCd;
	private String useYn;
	private String memo;
	private String regId;
	private LocalDateTime regDt;
	private String updateId;
	private LocalDateTime updateDt;
	private String companyNm;

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPrimaryYn() {
		return primaryYn;
	}

	public void setPrimaryYn(String primaryYn) {
		this.primaryYn = primaryYn;
	}

	public String getSslYn() {
		return sslYn;
	}

	public void setSslYn(String sslYn) {
		this.sslYn = sslYn;
	}

	public LocalDateTime getSslCertNotBefore() {
		return sslCertNotBefore;
	}

	public void setSslCertNotBefore(LocalDateTime sslCertNotBefore) {
		this.sslCertNotBefore = sslCertNotBefore;
	}

	public LocalDateTime getSslCertNotAfter() {
		return sslCertNotAfter;
	}

	public void setSslCertNotAfter(LocalDateTime sslCertNotAfter) {
		this.sslCertNotAfter = sslCertNotAfter;
	}

	public String getSslCertSubject() {
		return sslCertSubject;
	}

	public void setSslCertSubject(String sslCertSubject) {
		this.sslCertSubject = sslCertSubject;
	}

	public String getSslCertIssuer() {
		return sslCertIssuer;
	}

	public void setSslCertIssuer(String sslCertIssuer) {
		this.sslCertIssuer = sslCertIssuer;
	}

	public Long getSslCertFileId() {
		return sslCertFileId;
	}

	public void setSslCertFileId(Long sslCertFileId) {
		this.sslCertFileId = sslCertFileId;
	}

	public String getVerifyStatusCd() {
		return verifyStatusCd;
	}

	public void setVerifyStatusCd(String verifyStatusCd) {
		this.verifyStatusCd = verifyStatusCd;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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

	public String getCompanyNm() {
		return companyNm;
	}

	public void setCompanyNm(String companyNm) {
		this.companyNm = companyNm;
	}
}
