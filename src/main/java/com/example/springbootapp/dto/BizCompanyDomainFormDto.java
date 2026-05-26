package com.example.springbootapp.dto;

public class BizCompanyDomainFormDto {
	private Long domainId;
	private Long companyId;
	private String hostName;
	private String primaryYn;
	private String sslYn;
	private Long sslCertFileId;
	private String sslCertNotBefore;
	private String sslCertNotAfter;
	private String sslCertSubject;
	private String sslCertIssuer;
	private String verifyStatusCd;
	private String useYn;
	private String memo;

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

	public Long getSslCertFileId() {
		return sslCertFileId;
	}

	public void setSslCertFileId(Long sslCertFileId) {
		this.sslCertFileId = sslCertFileId;
	}

	public String getSslCertNotBefore() {
		return sslCertNotBefore;
	}

	public void setSslCertNotBefore(String sslCertNotBefore) {
		this.sslCertNotBefore = sslCertNotBefore;
	}

	public String getSslCertNotAfter() {
		return sslCertNotAfter;
	}

	public void setSslCertNotAfter(String sslCertNotAfter) {
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
}
