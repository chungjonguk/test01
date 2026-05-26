package com.example.springbootapp.dto;

import java.util.List;

public class BizCompanyDomainBulkDeleteDto {
	private List<Long> domainIds;

	public List<Long> getDomainIds() {
		return domainIds;
	}

	public void setDomainIds(List<Long> domainIds) {
		this.domainIds = domainIds;
	}
}
