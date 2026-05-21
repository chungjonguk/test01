package com.example.springbootapp.dto;

/**
 * 카카오 로컬 API 주소 검색 결과 (화면 자동 입력용).
 */
public record KakaoAddressItemDto(
		String displayAddress,
		String addressType,
		String postalCode,
		String addressLine1,
		String city,
		String state,
		String region3,
		String buildingName,
		Double longitude,
		Double latitude) {
}
