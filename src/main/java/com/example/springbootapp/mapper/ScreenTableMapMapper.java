package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.ScreenTableMap;
/**
 * 화면 URI–DB 테이블 매핑(screen_table_map) MyBatis Mapper.
 */
@Mapper
public interface ScreenTableMapMapper {
	/**
	 * URI 경로로 화면–테이블 매핑을 조회합니다.
	 *
	 * @param uriPath 요청 URI 경로
	 * @return 매핑 정보, 없으면 {@code null}
	 */
	ScreenTableMap findByUriPath(@Param("uriPath") String uriPath);
	/**
	 * 전체 화면–테이블 매핑 목록을 조회합니다.
	 *
	 * @return 매핑 목록
	 */
	List<ScreenTableMap> findAll();
	/**
	 * 화면–테이블 매핑을 등록하거나 갱신합니다.
	 *
	 * @param row 저장할 매핑 행
	 * @return 반영된 행 수
	 */
	int upsert(ScreenTableMap row);
}
