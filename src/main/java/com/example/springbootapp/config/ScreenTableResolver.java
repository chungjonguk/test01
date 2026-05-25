package com.example.springbootapp.config;
/**
 * URL 경로별 연동 테이블·데이터 유형을 결정합니다.
 */
public final class ScreenTableResolver {
	public record Mapping(String primaryTable, String relatedTables, char dataType, String tableDesc) {
	}
	private ScreenTableResolver() {
	}
	public static Mapping resolve(String uriPath) {
		if (uriPath == null || uriPath.isBlank()) {
			return staticUi(null, "경로 없음");
		}
		String uri = uriPath.endsWith("/") && uriPath.length() > 1
				? uriPath.substring(0, uriPath.length() - 1)
				: uriPath;
		if ("/users".equals(uri)) {
			return db("user", "user_access_log", "회원 목록·등록·접속이력");
		}
		if ("/admin/codes".equals(uri)) {
			return db("common_code", "common_code_value", "공통코드·상세코드 관리");
		}
		if ("/admin/dashboard-config".equals(uri)) {
			return db("dashboard_company_config", "biz_company", "업체별 대시보드 위젯 구성");
		}
		if ("/admin/menus".equals(uri)) {
			return db("screen_list", "screen_table_map", "화면·테이블 매핑 조회");
		}
		if ("/admin/user-access-logs".equals(uri)) {
			return db("user_access_log", "user", "사용자 접속 로그 조회");
		}
		if ("/admin/companies".equals(uri)) {
			return db("biz_company", null, "업체(거래처) 마스터 관리");
		}
		if ("/admin/company-page-images".equals(uri)) {
			return db("biz_company_page_image", "biz_company,nas_file", "업체별 페이지 이미지 등록");
		}
		if ("/admin/nas".equals(uri) || "/admin/media-storage".equals(uri)) {
			return db("nas_file", null, "NAS 설정·파일 저장");
		}
		if (uri.startsWith("/api/storage")) {
			return db("nas_file", null, "NAS 스토리지 API");
		}
		if (uri.startsWith("/dashboard")) {
			return codeOnly("common_code,common_code_value", "대시보드 콤보(공통코드)");
		}
		if (uri.contains("/authentication/simple/login") || uri.contains("/authentication/card/login")
				|| uri.contains("/authentication/split/login")) {
			return db("user", "user_access_log", "로그인(세션·접속이력)");
		}
		if (uri.contains("/authentication") && uri.contains("logout")) {
			return db("user_access_log", "user", "로그아웃(접속이력)");
		}
		if (uri.startsWith("/auth/")) {
			return db("user_access_log", "user", "인증 API(접속이력)");
		}
		if (uri.contains("kakao-callback") || uri.contains("naver-callback")) {
			return db("user_access_log", "user", "소셜 로그인 콜백(접속이력)");
		}
		if (uri.contains("/authentication") && uri.contains("register")) {
			return db("user", null, "회원가입 폼(UI)");
		}
		if (uri.startsWith("/pages/user/")) {
			return db("user", null, "프로필·설정(UI)");
		}
		if (uri.startsWith("/pages/pricing/")) {
			return db("pricing_plan", null, "요금제");
		}
		if (uri.startsWith("/pages/faq/")) {
			return db("faq_item", null, "FAQ");
		}
		if (uri.contains("/app/e-commerce/product/product-images")) {
			return db("ecm_product", "ecm_product_image", "상품 이미지 갤러리(최대 5)");
		}
		if (uri.contains("/app/e-commerce/product/product-register")) {
			return db("ecm_product", "ecm_product_image", "상품 등록·이미지 최대 5");
		}
		if (uri.contains("/app/e-commerce/product/product-manage")) {
			return db("ecm_product", null, "상품 관리·조회");
		}
		if (uri.startsWith("/app/e-commerce/product/")) {
			return db("ecm_product", "ecm_product_image", "상품 목록·그리드·상세");
		}
		if (uri.contains("/app/e-commerce/orders/order-list")) {
			return db("ecm_order", "ecm_customer,ecm_order_item", "주문 목록");
		}
		if (uri.contains("/app/e-commerce/orders/order-details")) {
			return db("ecm_order", "ecm_order_item,ecm_customer", "주문 상세");
		}
		if (uri.contains("/app/e-commerce/customers")) {
			return db("ecm_customer", null, "고객 목록");
		}
		if (uri.contains("/app/e-commerce/customer-details")) {
			return db("ecm_customer", "ecm_order", "고객 상세");
		}
		if (uri.contains("/app/e-commerce/shopping-cart")) {
			return db("ecm_cart_item", "ecm_product", "장바구니");
		}
		if (uri.contains("/app/e-commerce/checkout")) {
			return db("ecm_payment", "ecm_order,ecm_cart_item,ecm_customer", "결제(이니시스)");
		}
		if (uri.contains("/app/e-commerce/billing")) {
			return db("ecm_billing", "ecm_customer", "청구");
		}
		if (uri.contains("/app/e-commerce/invoice")) {
			return db("ecm_invoice", "ecm_order", "인보이스");
		}
		if (uri.contains("/app/e-learning/course/")) {
			return db("lms_course", "lms_enrollment", "강좌");
		}
		if (uri.contains("/app/e-learning/student-overview")) {
			return db("lms_enrollment", "lms_course", "수강 현황");
		}
		if (uri.contains("/app/e-learning/trainer-profile")) {
			return db("lms_trainer", "lms_course", "강사 프로필");
		}
		if (uri.startsWith("/app/email/")) {
			return db("email_message", null, "메일함");
		}
		if (uri.startsWith("/app/calendar") || uri.startsWith("/app/events/")) {
			return db("calendar_event", null, "일정·이벤트");
		}
		if (uri.contains("/app/social/feed")) {
			return db("social_post", null, "피드");
		}
		if (uri.contains("/app/social/activity-log")) {
			return db("social_activity", null, "활동 로그");
		}
		if (uri.contains("/app/social/notifications")) {
			return db("social_notification", null, "알림");
		}
		if (uri.contains("/app/social/followers")) {
			return db("social_follower", null, "팔로워");
		}
		if (uri.startsWith("/app/kanban")) {
			return db("kanban_board", "kanban_column,kanban_card", "칸반");
		}
		if (uri.startsWith("/app/chat")) {
			return db("chat_room", "chat_message", "채팅");
		}
		if (uri.startsWith("/modules/") || uri.startsWith("/demo/") || uri.startsWith("/documentation/")) {
			return staticUi(null, "Falcon 컴포넌트 데모(정적)");
		}
		if (uri.startsWith("/pages/authentication/") || uri.startsWith("/pages/errors/")
				|| uri.startsWith("/pages/miscellaneous/") || "/pages/starter".equals(uri)
				|| "/pages/landing".equals(uri)) {
			return staticUi(null, "인증·에러·랜딩(정적 UI)");
		}
		if ("/".equals(uri) || "/dashboard".equals(uri)) {
			return codeOnly("screen_list", "홈·대시보드 메타");
		}
		return staticUi(null, "정적 UI");
	}
	private static Mapping db(String primary, String related, String desc) {
		return new Mapping(primary, related, 'D', desc);
	}
	private static Mapping codeOnly(String related, String desc) {
		return new Mapping("common_code", related, 'C', desc);
	}
	private static Mapping staticUi(String related, String desc) {
		return new Mapping(null, related, 'S', desc);
	}
}
