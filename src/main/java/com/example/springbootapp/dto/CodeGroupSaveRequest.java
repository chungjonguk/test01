package com.example.springbootapp.dto;

import java.util.ArrayList;
import java.util.List;

public class CodeGroupSaveRequest {

	private List<CodeGroupSaveDto> groups = new ArrayList<>();

	public List<CodeGroupSaveDto> getGroups() {
		return groups;
	}

	public void setGroups(List<CodeGroupSaveDto> groups) {
		this.groups = groups != null ? groups : new ArrayList<>();
	}
}
