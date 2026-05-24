package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.NasFile;
/**
 * NAS 파일 메타데이터(nas_file) MyBatis Mapper.
 */
@Mapper
public interface NasFileMapper {
	/**
	 * NAS 파일 메타데이터를 등록합니다.
	 *
	 * @param row 등록할 파일 메타데이터
	 * @return 반영된 행 수
	 */
	int insert(NasFile row);
	/**
	 * 파일 ID로 단건 조회합니다.
	 *
	 * @param fileId 파일 ID
	 * @return 파일 메타데이터, 없으면 {@code null}
	 */
	NasFile findById(@Param("fileId") Long fileId);
	/**
	 * 미디어 유형별 NAS 파일 목록을 검색합니다.
	 *
	 * @param mediaTypeCd 미디어 유형 코드 (nullable)
	 * @param limit       최대 조회 건수
	 * @return 파일 메타데이터 목록
	 */
	List<NasFile> search(
			@Param("mediaTypeCd") String mediaTypeCd,
			@Param("limit") int limit);
}
