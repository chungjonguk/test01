package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.CommonCodeValue;
/**
 * 공통코드 상세값(common_code_value) MyBatis Mapper.
 */
@Mapper
public interface CommonCodeValueMapper {
	/**
	 * 코드 그룹 ID에 속한 상세 코드 목록을 조회합니다.
	 *
	 * @param codeId 코드 그룹 ID
	 * @return 상세 코드 목록
	 */
	List<CommonCodeValue> findByCodeId(@Param("codeId") String codeId);
	/**
	 * 여러 코드 그룹 ID에 속한 상세 코드를 일괄 조회합니다.
	 *
	 * @param codeIds 코드 그룹 ID 목록
	 * @return 상세 코드 목록
	 */
	List<CommonCodeValue> findByCodeIds(@Param("codeIds") List<String> codeIds);
	/**
	 * 코드 그룹 ID와 코드 값으로 단건 조회합니다.
	 *
	 * @param codeId  코드 그룹 ID
	 * @param codeVal 코드 값
	 * @return 상세 코드, 없으면 {@code null}
	 */
	CommonCodeValue findByCodeIdAndCodeVal(
			@Param("codeId") String codeId,
			@Param("codeVal") String codeVal);
	/**
	 * 상세 코드를 등록합니다.
	 *
	 * @param value 등록할 상세 코드
	 * @return 반영된 행 수
	 */
	int insert(CommonCodeValue value);
	/**
	 * 상세 코드를 수정합니다.
	 *
	 * @param value 수정할 상세 코드
	 * @return 반영된 행 수
	 */
	int update(CommonCodeValue value);
	/**
	 * 코드 그룹 ID에 속한 상세 코드를 모두 삭제합니다.
	 *
	 * @param codeId 코드 그룹 ID
	 * @return 삭제된 행 수
	 */
	int deleteByCodeId(@Param("codeId") String codeId);
	/**
	 * 여러 코드 그룹 ID에 속한 상세 코드를 일괄 삭제합니다.
	 *
	 * @param codeIds 코드 그룹 ID 목록
	 * @return 삭제된 행 수
	 */
	int deleteByCodeIds(@Param("codeIds") List<String> codeIds);
	/**
	 * 코드 그룹 ID와 코드 값으로 단건 삭제합니다.
	 *
	 * @param codeId  코드 그룹 ID
	 * @param codeVal 코드 값
	 * @return 삭제된 행 수
	 */
	int deleteByCodeIdAndCodeVal(@Param("codeId") String codeId, @Param("codeVal") String codeVal);
}
