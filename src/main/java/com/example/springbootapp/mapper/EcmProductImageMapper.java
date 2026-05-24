package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.EcmProductImage;
/**
 * EC 상품 이미지(ecm_product_image) MyBatis Mapper.
 */
@Mapper
public interface EcmProductImageMapper {
	/**
	 * 상품 ID에 연결된 이미지 목록을 조회합니다.
	 *
	 * @param productId 상품 ID
	 * @return 상품 이미지 목록
	 */
	List<EcmProductImage> findByProductId(@Param("productId") Long productId);
	/**
	 * 상품 ID에 연결된 이미지를 모두 삭제합니다.
	 *
	 * @param productId 상품 ID
	 * @return 삭제된 행 수
	 */
	int deleteByProductId(@Param("productId") Long productId);
	/**
	 * 상품 이미지를 등록합니다.
	 *
	 * @param image 등록할 상품 이미지
	 * @return 반영된 행 수
	 */
	int insert(EcmProductImage image);
}
