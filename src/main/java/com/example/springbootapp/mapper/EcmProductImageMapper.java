package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.EcmProductImage;

@Mapper
public interface EcmProductImageMapper {

	List<EcmProductImage> findByProductId(@Param("productId") Long productId);

	int deleteByProductId(@Param("productId") Long productId);

	int insert(EcmProductImage image);
}
