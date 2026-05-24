package com.example.springbootapp.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.springbootapp.dto.CodeOption;
import com.example.springbootapp.service.CommonCodeService;
/**
 * 공통코드 옵션 조회 REST API.
 * <p>기본 경로: {@code /api/codes}</p>
 */
@RestController
@RequestMapping("/api/codes")
public class CodeOptionsApiController {
	private final CommonCodeService commonCodeService;
	public CodeOptionsApiController(CommonCodeService commonCodeService) {
		this.commonCodeService = commonCodeService;
	}
	/**
	 * 활성화된 전체 코드 그룹의 옵션 맵을 조회합니다.
	 *
	 * @return out: {@code ResponseEntity<Map<String, List<CodeOption>>>} — 코드 그룹 ID별 옵션 목록
	 */
	@GetMapping("/options")
	public ResponseEntity<Map<String, List<CodeOption>>> allOptions() {
		return ResponseEntity.ok(commonCodeService.findAllActiveOptionsMap());
	}
	/**
	 * 특정 코드 그룹의 활성 옵션 목록을 조회합니다.
	 *
	 * @param codeId in: 코드 그룹 ID
	 * @return out: {@code ResponseEntity<Map>} — {@code codeId}, {@code options}, {@code count}
	 */
	@GetMapping("/options/{codeId}")
	public ResponseEntity<Map<String, Object>> optionsByGroup(@PathVariable String codeId) {
		List<CodeOption> options = commonCodeService.findActiveOptions(codeId);
		Map<String, Object> body = new HashMap<>();
		body.put("codeId", codeId);
		body.put("options", options);
		body.put("count", options.size());
		return ResponseEntity.ok(body);
	}
}
