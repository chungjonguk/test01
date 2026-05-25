package com.example.springbootapp.shipping;

/**
 * 택배사별 운송장 발급 클라이언트.
 */
public interface CarrierWaybillClient {

	CarrierCd carrier();

	WaybillIssueResult issue(WaybillIssueCommand command);
}
