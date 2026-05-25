package com.example.springbootapp.shipping;

/**
 * 운송장 발급 API 응답.
 */
public class WaybillIssueResult {
	private boolean success;
	private boolean mock;
	private String invoiceNo;
	private String requestPayload;
	private String responsePayload;
	private String message;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isMock() {
		return mock;
	}

	public void setMock(boolean mock) {
		this.mock = mock;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getRequestPayload() {
		return requestPayload;
	}

	public void setRequestPayload(String requestPayload) {
		this.requestPayload = requestPayload;
	}

	public String getResponsePayload() {
		return responsePayload;
	}

	public void setResponsePayload(String responsePayload) {
		this.responsePayload = responsePayload;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
