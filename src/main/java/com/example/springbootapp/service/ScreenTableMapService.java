package com.example.springbootapp.service;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.config.ScreenTableResolver;
import com.example.springbootapp.domain.ScreenList;
import com.example.springbootapp.domain.ScreenTableMap;
import com.example.springbootapp.mapper.ScreenTableMapMapper;
/**
 * 화면 URI와 연관 DB 테이블 매핑 정보 조회·동기화를 처리하는 서비스.
 */
@Service
public class ScreenTableMapService {
	private final ScreenTableMapMapper screenTableMapMapper;
	public ScreenTableMapService(ScreenTableMapMapper screenTableMapMapper) {
		this.screenTableMapMapper = screenTableMapMapper;
	}
	/**
	 * 모든 화면-테이블 매핑을 URI 경로 키 맵으로 조회한다.
	 *
	 * @return URI 경로 → 매핑 정보 맵
	 */
	public Map<String, ScreenTableMap> findAllByUri() {
		return screenTableMapMapper.findAll().stream()
				.filter(m -> m.getUriPath() != null && !m.getUriPath().isBlank())
				.collect(Collectors.toMap(ScreenTableMap::getUriPath, m -> m, (a, b) -> a, LinkedHashMap::new));
	}
	/**
	 * URI 경로로 화면-테이블 매핑을 조회한다.
	 *
	 * @param uriPath URI 경로
	 * @return 매핑 엔티티, 없으면 null
	 */
	public ScreenTableMap findByUriPath(String uriPath) {
		return screenTableMapMapper.findByUriPath(uriPath);
	}
	/**
	 * 화면 목록을 기준으로 URI-테이블 매핑 정보를 일괄 동기화(upsert)한다.
	 *
	 * @param screens 동기화할 화면 목록
	 * @param actor   등록·수정자 ID
	 */
	@Transactional
	public void syncFromScreens(List<ScreenList> screens, String actor) {
		LocalDateTime now = LocalDateTime.now();
		for (ScreenList screen : screens) {
			ScreenTableResolver.Mapping mapping = ScreenTableResolver.resolve(screen.getUriPath());
			ScreenTableMap row = new ScreenTableMap();
			row.setUriPath(screen.getUriPath());
			row.setScreenId(screen.getScreenId());
			row.setPrimaryTable(mapping.primaryTable());
			row.setRelatedTables(mapping.relatedTables());
			row.setDataType(String.valueOf(mapping.dataType()));
			row.setTableDesc(mapping.tableDesc());
			row.setRegId(actor);
			row.setRegDt(now);
			row.setUpdateId(actor);
			row.setUpdateDt(now);
			screenTableMapMapper.upsert(row);
		}
	}
}
