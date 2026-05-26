package com.example.springbootapp.config;

import java.util.ArrayList;
import java.util.List;

import com.example.springbootapp.domain.TableSequence;

/**
 * 프로젝트 테이블별 시퀀스 정의 카탈로그 (기동 시 {@code sys_table_sequence}에 등록).
 */
public final class TableSequenceCatalog {

	public record Entry(String tableName, String columnName, String description) {
	}

	private TableSequenceCatalog() {
	}

	public static List<Entry> all() {
		List<Entry> list = new ArrayList<>();
		add(list, "user_access_log", "access_id", "접속 이력");
		add(list, "biz_company", "company_id", "업체");
		add(list, "biz_company_page_image", "image_id", "업체 페이지 이미지");
		add(list, "biz_company_domain", "domain_id", "업체 도메인");
		add(list, "ecm_payment", "payment_id", "결제");
		add(list, "nas_file", "file_id", "NAS 파일");
		add(list, "ecm_product", "product_id", "상품");
		add(list, "ecm_product_image", "image_id", "상품 이미지");
		add(list, "ecm_customer", "customer_id", "고객");
		add(list, "ecm_order", "order_id", "주문");
		add(list, "ecm_shipment", "shipment_id", "배송·운송장");
		add(list, "ecm_cart_item", "cart_item_id", "장바구니");
		add(list, "ecm_invoice", "invoice_id", "인보이스");
		add(list, "ecm_billing", "billing_id", "청구");
		add(list, "lms_course", "course_id", "강좌");
		add(list, "lms_enrollment", "enrollment_id", "수강");
		add(list, "lms_trainer", "trainer_id", "강사");
		add(list, "email_message", "message_id", "메일");
		add(list, "calendar_event", "event_id", "일정");
		add(list, "social_post", "post_id", "소셜 게시");
		add(list, "social_activity", "activity_id", "소셜 활동");
		add(list, "social_notification", "notification_id", "알림");
		add(list, "social_follower", "follower_id", "팔로워");
		add(list, "kanban_board", "board_id", "칸반 보드");
		add(list, "kanban_column", "column_id", "칸반 컬럼");
		add(list, "kanban_card", "card_id", "칸반 카드");
		add(list, "chat_room", "room_id", "채팅방");
		add(list, "chat_message", "message_id", "채팅 메시지");
		add(list, "pricing_plan", "plan_id", "요금제");
		add(list, "faq_item", "faq_id", "FAQ");
		return list;
	}

	public static TableSequence toEntity(Entry entry) {
		TableSequence seq = new TableSequence();
		seq.setSeqName(entry.tableName());
		seq.setTableName(entry.tableName());
		seq.setColumnName(entry.columnName());
		seq.setNextVal(0L);
		seq.setIncrementBy(1);
		seq.setMinVal(1L);
		seq.setDescription(entry.description());
		seq.setUseYn("Y");
		seq.setRegId("SYSTEM");
		seq.setUpdateId("SYSTEM");
		return seq;
	}

	private static void add(List<Entry> list, String table, String column, String desc) {
		list.add(new Entry(table, column, desc));
	}
}
