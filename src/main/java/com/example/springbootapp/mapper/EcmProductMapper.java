package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.EcmProduct;

@Mapper
public interface EcmProductMapper {

	List<EcmProduct> findAll(@Param("productNm") String productNm, @Param("categoryCd") String categoryCd,
			@Param("statusCd") String statusCd);

	EcmProduct findById(@Param("productId") Long productId);

	int insert(EcmProduct product);

	int update(EcmProduct product);

	int deleteById(@Param("productId") Long productId);
}
