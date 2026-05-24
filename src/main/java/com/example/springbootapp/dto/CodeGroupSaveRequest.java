package com.example.springbootapp.dto;
import java.util.ArrayList;
import java.util.List;
/**
 * 공통코드 그룹 일괄 저장 API 요청 래퍼.
 * <p>in: 코드 그룹·상세 일괄 저장 request body</p>
 * <ul>
 *   <li>{@code groups} — in: 저장할 코드 그룹 목록 ({@link CodeGroupSaveDto})</li>
 * </ul>
 */
public class CodeGroupSaveRequest {
	private List<CodeGroupSaveDto> groups = new ArrayList<>();
	public List<CodeGroupSaveDto> getGroups() {
		return groups;
	}
	public void setGroups(List<CodeGroupSaveDto> groups) {
		this.groups = groups != null ? groups : new ArrayList<>();
	}
}
