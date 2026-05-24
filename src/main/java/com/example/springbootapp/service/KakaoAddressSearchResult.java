package com.example.springbootapp.service;
import java.util.List;
import com.example.springbootapp.dto.KakaoAddressItemDto;
public record KakaoAddressSearchResult(
		List<KakaoAddressItemDto> items,
		boolean mock,
		String warning) {
	public static KakaoAddressSearchResult live(List<KakaoAddressItemDto> items) {
		return new KakaoAddressSearchResult(items, false, null);
	}
	public static KakaoAddressSearchResult mockFallback(List<KakaoAddressItemDto> items, String warning) {
		return new KakaoAddressSearchResult(items, true, warning);
	}
}
