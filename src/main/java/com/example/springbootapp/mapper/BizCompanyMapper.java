package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.BizCompany;
/**
 * 거래처(회사, biz_company) MyBatis Mapper.
 */
@Mapper
public interface BizCompanyMapper {
	/**
	 * 조건에 맞는 거래처 목록을 검색합니다.
	 *
	 * @param companyNm 회사명 (부분 일치, nullable)
	 * @param bizNo     사업자번호 (nullable)
	 * @param statusCd  상태 코드 (nullable)
	 * @param useYn     사용 여부 (nullable)
	 * @param limit     최대 조회 건수
	 * @return 거래처 목록
	 */
	List<BizCompany> search(
			@Param("companyNm") String companyNm,
			@Param("bizNo") String bizNo,
			@Param("statusCd") String statusCd,
			@Param("useYn") String useYn,
			@Param("limit") int limit);
	/**
	 * 거래처 ID로 단건 조회합니다.
	 *
	 * @param companyId 거래처 ID
	 * @return 거래처, 없으면 {@code null}
	 */
	BizCompany findById(@Param("companyId") Long companyId);
	/**
	 * 거래처를 등록합니다.
	 *
	 * @param company 등록할 거래처
	 * @return 반영된 행 수
	 */
	int insert(BizCompany company);
	/**
	 * 거래처를 수정합니다.
	 *
	 * @param company 수정할 거래처
	 * @return 반영된 행 수
	 */
	int update(BizCompany company);
	/**
	 * 거래처 ID로 단건 삭제합니다.
	 *
	 * @param companyId 거래처 ID
	 * @return 삭제된 행 수
	 */
	int deleteById(@Param("companyId") Long companyId);
}
