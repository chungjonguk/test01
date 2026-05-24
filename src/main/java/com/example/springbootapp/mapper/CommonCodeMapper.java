package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.CommonCode;
/**
 * 공통코드 그룹(common_code) MyBatis Mapper.
 */
@Mapper
public interface CommonCodeMapper {
	/**
	 * 조건에 맞는 코드 그룹 목록을 검색합니다.
	 *
	 * @param codeId 코드 그룹 ID (부분 일치, nullable)
	 * @param codeNm 코드 그룹명 (부분 일치, nullable)
	 * @param useYn  사용 여부 (nullable)
	 * @return 검색된 코드 그룹 목록
	 */
	List<CommonCode> search(
			@Param("codeId") String codeId,
			@Param("codeNm") String codeNm,
			@Param("useYn") String useYn);
	/**
	 * 코드 그룹 ID로 단건 조회합니다.
	 *
	 * @param codeId 코드 그룹 ID
	 * @return 코드 그룹, 없으면 {@code null}
	 */
	CommonCode findByCodeId(@Param("codeId") String codeId);
	/**
	 * 코드 그룹을 등록합니다.
	 *
	 * @param group 등록할 코드 그룹
	 * @return 반영된 행 수
	 */
	int insert(CommonCode group);
	/**
	 * 코드 그룹을 수정합니다.
	 *
	 * @param group 수정할 코드 그룹
	 * @return 반영된 행 수
	 */
	int update(CommonCode group);
	/**
	 * 코드 그룹 ID로 단건 삭제합니다.
	 *
	 * @param codeId 코드 그룹 ID
	 * @return 삭제된 행 수
	 */
	int deleteByCodeId(@Param("codeId") String codeId);
	/**
	 * 코드 그룹 ID 목록으로 일괄 삭제합니다.
	 *
	 * @param codeIds 삭제할 코드 그룹 ID 목록
	 * @return 삭제된 행 수
	 */
	int deleteByCodeIds(@Param("codeIds") List<String> codeIds);
}
