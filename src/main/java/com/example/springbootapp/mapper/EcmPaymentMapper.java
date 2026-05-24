package com.example.springbootapp.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.EcmPayment;
/**
 * EC 결제(ecm_payment) MyBatis Mapper.
 */
@Mapper
public interface EcmPaymentMapper {
	/**
	 * 결제 정보를 등록합니다.
	 *
	 * @param payment 등록할 결제
	 * @return 반영된 행 수
	 */
	int insert(EcmPayment payment);
	/**
	 * 주문번호로 결제 정보를 조회합니다.
	 *
	 * @param orderNo 주문번호
	 * @return 결제 정보, 없으면 {@code null}
	 */
	EcmPayment findByOrderNo(@Param("orderNo") String orderNo);
	/**
	 * 인증(승인 요청) 완료 후 결제 정보를 갱신합니다.
	 *
	 * @param payment 갱신할 결제 정보
	 * @return 반영된 행 수
	 */
	int updateAfterAuth(EcmPayment payment);
	/**
	 * 최종 승인 완료 후 결제 정보를 갱신합니다.
	 *
	 * @param payment 갱신할 결제 정보
	 * @return 반영된 행 수
	 */
	int updateAfterApprove(EcmPayment payment);
	/**
	 * 결제 레코드에 주문 ID를 연결합니다.
	 *
	 * @param payment 주문 ID가 포함된 결제 정보
	 * @return 반영된 행 수
	 */
	int updateOrderId(EcmPayment payment);
}
