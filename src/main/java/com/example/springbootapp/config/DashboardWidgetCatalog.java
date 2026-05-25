package com.example.springbootapp.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 프로젝트에서 홈 대시보드에 배치 가능한 위젯 카탈로그.
 * <p>각 위젯 HTML은 {@code index.html} 내 {@code #dashboard-widget-templates} 에 정의되어 있어야 합니다.</p>
 */
public final class DashboardWidgetCatalog {

	public record WidgetDef(
			String id,
			String label,
			String description,
			String category,
			String colClass,
			boolean defaultEnabled,
			int sortOrder) {
	}

	private static final List<WidgetDef> WIDGETS = List.of(
			// 기본(홈)
			def("weekly-sales", "주간 매출", "주간 매출 막대 차트", "기본", "col-md-6 col-xxl-3", true, 10),
			def("total-order", "총 주문", "주문 건수 추이", "기본", "col-md-6 col-xxl-3", true, 20),
			def("market-share", "시장 점유율", "제조사별 점유율", "기본", "col-md-6 col-xxl-3", true, 30),
			def("weather", "날씨", "날씨 요약", "기본", "col-md-6 col-xxl-3", true, 40),
			def("new-users", "신규 사용자", "신규 가입 추이", "기본", "col-md-6 col-xxl-3", false, 45),
			def("running-projects", "진행 프로젝트", "프로젝트 진행률", "기본", "col-lg-6", true, 50),
			def("total-sales", "총 매출", "월별 매출 차트", "기본", "col-lg-6", true, 60),
			def("using-storage", "스토리지 사용량", "NAS 실사용량", "NAS·인프라", "col-lg-6 col-xl-7 col-xxl-8", true, 70),
			def("storage-upgrade", "스토리지 업그레이드", "용량 업그레이드 안내", "NAS·인프라", "col-lg-6 col-xl-5 col-xxl-4", true, 80),
			def("best-selling-products", "베스트 상품", "판매 상위 상품", "이커머스", "col-lg-7 col-xl-8", true, 90),
			def("shared-files", "공유 파일", "최근 공유 파일", "기본", "col-lg-5 col-xl-4", true, 100),
			def("active-users", "활성 사용자", "접속 중 사용자", "기본", "col-sm-6 col-xxl-3", true, 110),
			def("bandwidth-saved", "대역폭 절약", "대역폭 절약 차트", "기본", "col-sm-6 col-xxl-3", true, 120),
			def("top-products", "인기 상품", "인기 상품 바 차트", "이커머스", "col-xxl-6", true, 130),
			// 프로젝트 바로가기·요약
			def("admin-quick-links", "관리 바로가기", "메뉴·NAS·접속 로그 링크", "관리", "col-md-6 col-xxl-4", false, 200),
			def("ecm-quick-links", "이커머스 바로가기", "상품·주문 화면 링크", "이커머스", "col-md-6 col-xxl-4", false, 210),
			def("dashboard-summary", "대시보드 안내", "구성·미리보기 안내", "관리", "col-12", false, 220));

	private static WidgetDef def(
			String id, String label, String description, String category,
			String colClass, boolean defaultEnabled, int sortOrder) {
		return new WidgetDef(id, label, description, category, colClass, defaultEnabled, sortOrder);
	}

	private DashboardWidgetCatalog() {
	}

	public static List<WidgetDef> all() {
		return WIDGETS;
	}

	public static List<String> defaultEnabledIds() {
		List<String> ids = new ArrayList<>();
		for (WidgetDef w : WIDGETS) {
			if (w.defaultEnabled()) {
				ids.add(w.id());
			}
		}
		return ids;
	}

	public static List<Map<String, Object>> toMaps(List<WidgetDef> widgets) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (WidgetDef w : widgets) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", w.id());
			row.put("label", w.label());
			row.put("description", w.description());
			row.put("category", w.category());
			row.put("colClass", w.colClass());
			row.put("defaultEnabled", w.defaultEnabled());
			row.put("sortOrder", w.sortOrder());
			rows.add(row);
		}
		return rows;
	}
}
