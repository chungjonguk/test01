package com.example.springbootapp.dto;

import java.util.ArrayList;
import java.util.List;

public class CodeGroupSaveDto {

	private String codeId;
	private String codeNm;
	private String useYn;
	private List<CodeDetailSaveDto> codes = new ArrayList<>();

	public String getCodeId() {
		return codeId;
	}

	public void setCodeId(String codeId) {
		this.codeId = codeId;
	}

	public String getCodeNm() {
		return codeNm;
	}

	public void setCodeNm(String codeNm) {
		this.codeNm = codeNm;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	public List<CodeDetailSaveDto> getCodes() {
		return codes;
	}

	public void setCodes(List<CodeDetailSaveDto> codes) {
		this.codes = codes != null ? codes : new ArrayList<>();
	}
}
