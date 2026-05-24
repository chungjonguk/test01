package com.example.springbootapp.service;
/**
 * 카카오 앱에서 지도/로컬(OPEN_MAP_AND_LOCAL) 제품이 비활성화된 경우.
 */
public class KakaoLocalApiDisabledException extends RuntimeException {
	public static final String USER_MESSAGE =
			"카카오 개발자 콘솔에서 [지도/로컬] API를 활성화해 주세요. "
					+ "(developers.kakao.com → 내 애플리케이션 → 제품 설정 → 지도/로컬 → ON)";
	public KakaoLocalApiDisabledException() {
		super(USER_MESSAGE);
	}
}
