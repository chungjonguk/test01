package com.example.springbootapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.EcmOrder;

@Mapper
public interface EcmOrderMapper {

	int insert(EcmOrder order);

	EcmOrder findByOrderNo(@Param("orderNo") String orderNo);

	int updateStatusByOrderNo(
			@Param("orderNo") String orderNo,
			@Param("statusCd") String statusCd,
			@Param("updateId") String updateId);

	int updateOrderIdOnPayment(
			@Param("orderNo") String orderNo,
			@Param("orderId") Long orderId);
}
