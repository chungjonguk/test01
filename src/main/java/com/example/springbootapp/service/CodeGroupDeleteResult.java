package com.example.springbootapp.service;

public class CodeGroupDeleteResult {

	private final int deletedGroups;
	private final int deletedDetails;

	public CodeGroupDeleteResult(int deletedGroups, int deletedDetails) {
		this.deletedGroups = deletedGroups;
		this.deletedDetails = deletedDetails;
	}

	public int getDeletedGroups() {
		return deletedGroups;
	}

	public int getDeletedDetails() {
		return deletedDetails;
	}

	public String getMessage() {
		if (deletedGroups == 0 && deletedDetails == 0) {
			return "삭제된 항목이 없습니다.";
		}
		return "코드그룹 " + deletedGroups + "건, 상세코드 " + deletedDetails + "건이 삭제되었습니다.";
	}
}
