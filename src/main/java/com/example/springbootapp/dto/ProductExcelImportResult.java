package com.example.springbootapp.dto;
import java.util.ArrayList;
import java.util.List;
/**
 * 상품 엑셀 일괄 import 처리 결과.
 * <p>out: API 응답 body — 처리 건수 및 오류 메시지 목록</p>
 * <ul>
 *   <li>{@code created} — out: 신규 등록 건수</li>
 *   <li>{@code updated} — out: 수정 건수</li>
 *   <li>{@code skipped} — out: 건너뛴 건수</li>
 *   <li>{@code errors} — out: 행별 오류 메시지 목록</li>
 * </ul>
 */
public class ProductExcelImportResult {
	private int created;
	private int updated;
	private int skipped;
	private final List<String> errors = new ArrayList<>();
	public int getCreated() {
		return created;
	}
	public void setCreated(int created) {
		this.created = created;
	}
	public int getUpdated() {
		return updated;
	}
	public void setUpdated(int updated) {
		this.updated = updated;
	}
	public int getSkipped() {
		return skipped;
	}
	public void setSkipped(int skipped) {
		this.skipped = skipped;
	}
	public List<String> getErrors() {
		return errors;
	}
	public void addError(String message) {
		errors.add(message);
	}
	public void incrementCreated() {
		created++;
	}
	public void incrementUpdated() {
		updated++;
	}
	public void incrementSkipped() {
		skipped++;
	}
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	@Override
	public String toString() {
		return "ProductExcelImportResult{created=" + created + ", updated=" + updated + ", skipped=" + skipped
				+ ", errors=" + errors + "}";
	}
}
