package com.example.springbootapp.config;

import java.util.List;

/**
 * 서버 기동 시 적용할 classpath SQL 스크립트 실행 순서.
 * <p>새 DDL/시드 파일 추가 시 이 목록에 경로를 등록합니다.</p>
 */
public final class SchemaScriptCatalog {

	private static final List<String> STARTUP_SCRIPTS = List.of(
			// 시퀀스·난수 ID 마스터
			"schema/sys_table_sequence.sql",
			"schema/sys_table_random_id.sql",
			// 공통·화면 메타
			"schema/common_code.sql",
			"schema/screen_table_map.sql",
			"schema/screen_list.sql",
			"schema/screen_list_alter.sql",
			"schema/screen_list_charset.sql",
			// 업무·접속
			"schema/user_access_log.sql",
			"schema/user_access_log_alter.sql",
			"schema/nas_file.sql",
			"schema/biz_company.sql",
			"schema/dashboard_company_config.sql",
			"schema/biz_company_page_image.sql",
			"schema/biz_company_domain.sql",
			"schema/biz_company_domain_ssl_alter.sql",
			"schema/ecm_payment.sql",
			"schema/tables/biz_schema.sql",
			"schema/ecm_product_company_id.sql",
			"schema/auth_user_role.sql",
			"schema/user_profile_images.sql",
			"schema/user_profile_addresses.sql",
			"schema/user_profile_work_company.sql",
			"schema/social_notification_alter.sql",
			"schema/ecm_shipment.sql",
			"schema/tables/ecm_product_image.sql",
			// 샘플·시드 데이터
			"schema/tables/biz_seed.sql",
			"schema/common_code_charset.sql",
			"schema/common_code_seed.sql",
			"schema/common_code_combo_seed.sql",
			"schema/common_code_admin_screen_seed.sql",
			"schema/shipping_common_code_seed.sql",
			"schema/screen_list_admin_companies_menu.sql",
			"schema/screen_list_admin_company_section.sql",
			"schema/screen_list_admin_company_page_images.sql",
			"schema/screen_list_admin_inventory_menu.sql",
			"schema/screen_list_admin_shipping_menu.sql",
			"schema/screen_list_shopping_mall_menu.sql",
			"schema/screen_list_admin_company_domains.sql",
			"schema/screen_list_admin_company_customer_menus.sql",
			"schema/auth_user_role_seed.sql",
			"schema/screen_list_admin_table_sequences.sql",
			"schema/screen_list_social_notification_list.sql",
			"schema/screen_list_remove_pricing_menu.sql",
			"schema/screen_list_remove_documentation_menu.sql",
			"schema/screen_list_remove_modules_menu.sql",
			"schema/screen_list_remove_admin_settings_menu.sql",
			"schema/sys_table_sequence_seed.sql");

	private SchemaScriptCatalog() {
	}

	public static List<String> startupScripts() {
		return STARTUP_SCRIPTS;
	}
}
