package com.example.springbootapp.dto;
import java.util.ArrayList;
import java.util.List;
/**
 * 공통코드 그룹 저장 항목.
 * <p>in: {@link CodeGroupSaveRequest} 내부 그룹 단위 데이터</p>
 * <ul>
 *   <li>{@code codeId} — in: 코드 그룹 ID</li>
 *   <li>{@code codeNm} — in: 코드 그룹명</li>
 *   <li>{@code useYn} — in: 사용 여부</li>
 *   <li>{@code codes} — in: 하위 상세 코드 목록 ({@link CodeDetailSaveDto})</li>
 * </ul>
 */
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
