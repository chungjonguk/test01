package com.example.springbootapp.config.web;

/**
 * Host 테넌트 필터가 요청에 심는 속성 키.
 */
public final class CompanyHostTenantContext {

	public static final String ATTR_RESOLVED_HOST = "printmall.tenant.resolvedHost";
	public static final String ATTR_COMPANY_ID = "printmall.tenant.companyId";
	public static final String ATTR_COMPANY_NAME = "printmall.tenant.companyNm";

	private CompanyHostTenantContext() {
	}
}
