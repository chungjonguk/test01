package com.example.springbootapp.dto;
/**
 * 카카오 로컬 API 주소 검색 결과 (화면 자동 입력용).
 * <p>out: 주소 검색 API 응답·폼 자동완성 필드</p>
 * <ul>
 *   <li>{@code displayAddress} — out: 화면 표시용 전체 주소</li>
 *   <li>{@code addressType} — out: 주소 유형 (도로명/지번 등)</li>
 *   <li>{@code postalCode} — out: 우편번호</li>
 *   <li>{@code addressLine1} — out: 기본 주소</li>
 *   <li>{@code city} — out: 시·군·구</li>
 *   <li>{@code state} — out: 시·도</li>
 *   <li>{@code region3} — out: 읍·면·동</li>
 *   <li>{@code buildingName} — out: 건물명</li>
 *   <li>{@code longitude} — out: 경도</li>
 *   <li>{@code latitude} — out: 위도</li>
 * </ul>
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
