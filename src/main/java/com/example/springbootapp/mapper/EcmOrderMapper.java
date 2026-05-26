package com.example.springbootapp.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.example.springbootapp.domain.EcmOrder;
import com.example.springbootapp.dto.EcmOrderListItem;
/**
 * EC 주문(ecm_order) MyBatis Mapper.
 */
@Mapper
public interface EcmOrderMapper {
	/**
	 * 주문을 등록합니다.
	 *
	 * @param order 등록할 주문
	 * @return 반영된 행 수
	 */
	int insert(EcmOrder order);
	/**
	 * 주문번호로 단건 조회합니다.
	 *
	 * @param orderNo 주문번호
	 * @return 주문, 없으면 {@code null}
	 */
	EcmOrder findByOrderNo(@Param("orderNo") String orderNo);

	EcmOrder findById(@Param("orderId") Long orderId);
	/**
	 * 주문번호 기준으로 주문 상태를 갱신합니다.
	 *
	 * @param orderNo  주문번호
	 * @param statusCd 변경할 상태 코드
	 * @param updateId 수정자 ID
	 * @return 반영된 행 수
	 */
	int updateStatusByOrderNo(
			@Param("orderNo") String orderNo,
			@Param("statusCd") String statusCd,
			@Param("updateId") String updateId);

	int updateStatusByOrderId(
			@Param("orderId") Long orderId,
			@Param("statusCd") String statusCd,
			@Param("updateId") String updateId);
	/**
	 * 결제 완료 후 주문 ID를 연결합니다.
	 *
	 * @param orderNo 주문번호
	 * @param orderId 연결할 주문 ID
	 * @return 반영된 행 수
	 */
	int updateOrderIdOnPayment(
			@Param("orderNo") String orderNo,
			@Param("orderId") Long orderId);

	List<EcmOrderListItem> searchOrders(
			@Param("keyword") String keyword,
			@Param("statusCd") String statusCd);
}
