package com.example.springbootapp.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 업체별 등록 가능한 페이지 이미지 슬롯 정의.
 */
public final class CompanyPageImageCatalog {

	public record Slot(String pageCd, String label, String description, String hint) {
	}

	private static final List<Slot> SLOTS = List.of(
			new Slot("HOME_HERO", "홈 메인 배너", "대시보드(홈) 상단 히어로 영역", "권장 1920×600, jpg/png/webp"),
			new Slot("HOME_SIDE", "홈 보조 배너", "대시보드 보조 프로모션 영역", "권장 800×400"),
			new Slot("LOGIN_BG", "로그인 배경", "로그인 화면 배경 이미지", "권장 1920×1080"),
			new Slot("LOGO", "로고", "상단·사이드바 브랜드 로고", "권장 240×80, png/svg"),
			new Slot("SIDEBAR_ICON", "사이드 아이콘", "축소 내비게이션용 아이콘", "권장 64×64, png"));

	private CompanyPageImageCatalog() {
	}

	public static List<Slot> all() {
		return SLOTS;
	}

	public static Optional<Slot> find(String pageCd) {
		if (pageCd == null || pageCd.isBlank()) {
			return Optional.empty();
		}
		String code = pageCd.trim().toUpperCase();
		return SLOTS.stream().filter(s -> s.pageCd().equals(code)).findFirst();
	}

	public static List<Map<String, Object>> toMaps() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Slot slot : SLOTS) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("pageCd", slot.pageCd());
			row.put("label", slot.label());
			row.put("description", slot.description());
			row.put("hint", slot.hint());
			list.add(row);
		}
		return list;
	}
}
