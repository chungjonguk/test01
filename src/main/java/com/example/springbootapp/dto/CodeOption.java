package com.example.springbootapp.dto;
/**
 * 공통코드·셀렉트박스 옵션 항목.
 * <p>out: API 응답·화면 드롭다운 option (value/label 쌍)</p>
 * <ul>
 *   <li>{@code value} — out: 코드 값</li>
 *   <li>{@code label} — out: 표시명</li>
 * </ul>
 */
public class CodeOption {
	private final String value;
	private final String label;
	public CodeOption(String value, String label) {
		this.value = value;
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public String getLabel() {
		return label;
	}
	@Override
	public String toString() {
		return "CodeOption{value='" + value + "', label='" + label + "'}";
	}
}
