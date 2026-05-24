package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.EcmProduct;
/**
 * EC 상품(ecm_product) MyBatis Mapper.
 */
@Mapper
public interface EcmProductMapper {
	/**
	 * 조건에 맞는 상품 목록을 조회합니다.
	 *
	 * @param productNm  상품명 (부분 일치, nullable)
	 * @param categoryCd 카테고리 코드 (nullable)
	 * @param statusCd   판매 상태 코드 (nullable)
	 * @return 상품 목록
	 */
	List<EcmProduct> findAll(@Param("productNm") String productNm, @Param("categoryCd") String categoryCd,
			@Param("statusCd") String statusCd);
	/**
	 * 상품 ID로 단건 조회합니다.
	 *
	 * @param productId 상품 ID
	 * @return 상품, 없으면 {@code null}
	 */
	EcmProduct findById(@Param("productId") Long productId);
	/**
	 * 상품을 등록합니다.
	 *
	 * @param product 등록할 상품
	 * @return 반영된 행 수
	 */
	int insert(EcmProduct product);
	/**
	 * 상품을 수정합니다.
	 *
	 * @param product 수정할 상품
	 * @return 반영된 행 수
	 */
	int update(EcmProduct product);
	/**
	 * 상품 ID로 단건 삭제합니다.
	 *
	 * @param productId 상품 ID
	 * @return 삭제된 행 수
	 */
	int deleteById(@Param("productId") Long productId);
}
