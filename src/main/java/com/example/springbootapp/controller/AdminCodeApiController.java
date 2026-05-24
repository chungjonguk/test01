package com.example.springbootapp.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.dto.CodeGroupSaveRequest;
import com.example.springbootapp.service.CommonCodeService;
import jakarta.servlet.http.HttpSession;
/**
 * 관리자 공통코드 REST API.
 * <p>기본 경로: {@code /api/admin/codes}</p>
 */
@RestController
@RequestMapping("/api/admin/codes")
public class AdminCodeApiController {
	private final CommonCodeService commonCodeService;
	public AdminCodeApiController(CommonCodeService commonCodeService) {
		this.commonCodeService = commonCodeService;
	}
	/**
	 * 공통코드 그룹을 조건에 따라 검색합니다.
	 *
	 * @param codeId in: 코드 그룹 ID (선택)
	 * @param codeNm in: 코드 그룹명 (선택)
	 * @param useYn  in: 사용 여부 Y/N (선택)
	 * @return out: {@code ResponseEntity<Map>} — {@code groups}, {@code count}
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(required = false) String codeId,
			@RequestParam(required = false) String codeNm,
			@RequestParam(required = false) String useYn) {
		List<Map<String, Object>> groups = commonCodeService.searchGroups(codeId, codeNm, useYn);
		Map<String, Object> body = new HashMap<>();
		body.put("groups", groups);
		body.put("count", groups.size());
		return ResponseEntity.ok(body);
	}
	/**
	 * 공통코드 그룹 및 상세코드를 일괄 저장합니다.
	 *
	 * @param request in: 저장할 코드 그룹·상세 목록
	 * @param session in: 등록자 식별용 HTTP 세션
	 * @return out: {@code ResponseEntity<Map>} — {@code savedGroups}, {@code savedDetails}, {@code message}
	 */
	@PostMapping("/save")
	public ResponseEntity<Map<String, Object>> save(
			@RequestBody CodeGroupSaveRequest request,
			HttpSession session) {
		var result = commonCodeService.saveGroups(request, session);
		Map<String, Object> body = new HashMap<>();
		body.put("savedGroups", result.getSavedGroups());
		body.put("savedDetails", result.getSavedDetails());
		body.put("message", result.getMessage());
		return ResponseEntity.ok(body);
	}
	/**
	 * 공통코드 그룹을 다건 삭제합니다.
	 *
	 * @param codeIds in: 삭제할 코드 그룹 ID 목록
	 * @return out: {@code ResponseEntity<Map>} — {@code deleted}, {@code deletedGroups}, {@code deletedDetails}, {@code message}
	 */
	@DeleteMapping("/groups")
	public ResponseEntity<Map<String, Object>> deleteGroups(@RequestParam List<String> codeIds) {
		var result = commonCodeService.deleteGroups(codeIds);
		Map<String, Object> body = new HashMap<>();
		body.put("deleted", result.getDeletedGroups());
		body.put("deletedGroups", result.getDeletedGroups());
		body.put("deletedDetails", result.getDeletedDetails());
		body.put("message", result.getMessage());
		return ResponseEntity.ok(body);
	}
	/**
	 * 특정 그룹의 상세코드 값을 다건 삭제합니다.
	 *
	 * @param codeId   in: 코드 그룹 ID
	 * @param codeVals in: 삭제할 상세코드 값 목록
	 * @return out: {@code ResponseEntity<Map>} — {@code deleted}, {@code codeId}, {@code message}
	 */
	@DeleteMapping("/{codeId}/values")
	public ResponseEntity<Map<String, Object>> deleteCodeValues(
			@PathVariable String codeId,
			@RequestParam List<String> codeVals) {
		int deleted = commonCodeService.deleteCodeValues(codeId, codeVals);
		Map<String, Object> body = new HashMap<>();
		body.put("deleted", deleted);
		body.put("codeId", codeId);
		body.put("message", deleted + "건의 상세코드가 삭제되었습니다.");
		return ResponseEntity.ok(body);
	}
}
