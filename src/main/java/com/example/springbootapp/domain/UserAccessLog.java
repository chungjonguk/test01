package com.example.springbootapp.domain;
import java.time.LocalDateTime;
public class UserAccessLog {
	private Long accessId;
	private String userId;
	private String userNm;
	private String accessTypeCd;
	private String loginTypeCd;
	private String successYn;
	private String requestUri;
	private String httpMethod;
	private String clientIp;
	private String deviceTypeCd;
	private String deviceOs;
	private String deviceBrowser;
	private String deviceModel;
	private String userAgent;
	private String sessionId;
	private String failReason;
	private LocalDateTime accessDt;
	private String regId;
	public Long getAccessId() {
		return accessId;
	}
	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserNm() {
		return userNm;
	}
	public void setUserNm(String userNm) {
		this.userNm = userNm;
	}
	public String getAccessTypeCd() {
		return accessTypeCd;
	}
	public void setAccessTypeCd(String accessTypeCd) {
		this.accessTypeCd = accessTypeCd;
	}
	public String getLoginTypeCd() {
		return loginTypeCd;
	}
	public void setLoginTypeCd(String loginTypeCd) {
		this.loginTypeCd = loginTypeCd;
	}
	public String getSuccessYn() {
		return successYn;
	}
	public void setSuccessYn(String successYn) {
		this.successYn = successYn;
	}
	public String getRequestUri() {
		return requestUri;
	}
	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public String getDeviceTypeCd() {
		return deviceTypeCd;
	}
	public void setDeviceTypeCd(String deviceTypeCd) {
		this.deviceTypeCd = deviceTypeCd;
	}
	public String getDeviceOs() {
		return deviceOs;
	}
	public void setDeviceOs(String deviceOs) {
		this.deviceOs = deviceOs;
	}
	public String getDeviceBrowser() {
		return deviceBrowser;
	}
	public void setDeviceBrowser(String deviceBrowser) {
		this.deviceBrowser = deviceBrowser;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getFailReason() {
		return failReason;
	}
	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	public LocalDateTime getAccessDt() {
		return accessDt;
	}
	public void setAccessDt(LocalDateTime accessDt) {
		this.accessDt = accessDt;
	}
	public String getRegId() {
		return regId;
	}
	public void setRegId(String regId) {
		this.regId = regId;
	}
	@Override
	public String toString() {
		return "UserAccessLog{accessId=" + accessId + ", userId='" + userId + "', userNm='" + userNm
				+ "', accessTypeCd='" + accessTypeCd + "', loginTypeCd='" + loginTypeCd + "', successYn='" + successYn
				+ "', requestUri='" + requestUri + "', httpMethod='" + httpMethod + "', clientIp='" + clientIp
				+ "', deviceTypeCd='" + deviceTypeCd + "', deviceOs='" + deviceOs + "', deviceBrowser='" + deviceBrowser
				+ "', deviceModel='" + deviceModel + "', userAgent='" + userAgent + "', sessionId='" + sessionId
				+ "', failReason='" + failReason + "', accessDt=" + accessDt + ", regId='" + regId + "'}";
	}
}
