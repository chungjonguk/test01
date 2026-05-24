package com.example.springbootapp.dto;
/**
 * 공통코드 상세(값) 저장 항목.
 * <p>in: {@link CodeGroupSaveDto} 내부 상세 코드 단위 데이터</p>
 * <ul>
 *   <li>{@code codeVal} — in: 코드 값</li>
 *   <li>{@code useYn} — in: 사용 여부</li>
 * </ul>
 */
public class CodeDetailSaveDto {
	private String codeVal;
	private String useYn;
	public String getCodeVal() {
		return codeVal;
	}
	public void setCodeVal(String codeVal) {
		this.codeVal = codeVal;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	@Override
	public String toString() {
		return "CodeDetailSaveDto{codeVal='" + codeVal + "', useYn='" + useYn + "'}";
	}
}
