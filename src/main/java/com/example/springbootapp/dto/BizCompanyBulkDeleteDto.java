package com.example.springbootapp.dto;

import java.util.List;

public class BizCompanyBulkDeleteDto {

	private List<Long> companyIds;

	public List<Long> getCompanyIds() {
		return companyIds;
	}

	public void setCompanyIds(List<Long> companyIds) {
		this.companyIds = companyIds;
	}
}
