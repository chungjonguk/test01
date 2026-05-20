package com.example.springbootapp.dto;

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
}
