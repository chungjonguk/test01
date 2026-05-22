package com.example.springbootapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.EcmPayment;

@Mapper
public interface EcmPaymentMapper {

	int insert(EcmPayment payment);

	EcmPayment findByOrderNo(@Param("orderNo") String orderNo);

	int updateAfterAuth(EcmPayment payment);

	int updateAfterApprove(EcmPayment payment);

	int updateOrderId(EcmPayment payment);
}
