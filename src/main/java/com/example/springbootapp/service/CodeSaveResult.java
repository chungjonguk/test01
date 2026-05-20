package com.example.springbootapp.service;

public class CodeSaveResult {

	private final int savedGroups;
	private final int savedDetails;

	public CodeSaveResult(int savedGroups, int savedDetails) {
		this.savedGroups = savedGroups;
		this.savedDetails = savedDetails;
	}

	public int getSavedGroups() {
		return savedGroups;
	}

	public int getSavedDetails() {
		return savedDetails;
	}

	public String getMessage() {
		if (savedGroups == 0 && savedDetails == 0) {
			return "저장된 항목이 없습니다.";
		}
		return "코드그룹 " + savedGroups + "건, 상세코드 " + savedDetails + "건이 저장되었습니다.";
	}
}
