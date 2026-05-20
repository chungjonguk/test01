package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.CommonCodeValue;

@Mapper
public interface CommonCodeValueMapper {

	List<CommonCodeValue> findByCodeId(@Param("codeId") String codeId);

	List<CommonCodeValue> findByCodeIds(@Param("codeIds") List<String> codeIds);

	CommonCodeValue findByCodeIdAndCodeVal(
			@Param("codeId") String codeId,
			@Param("codeVal") String codeVal);

	int insert(CommonCodeValue value);

	int update(CommonCodeValue value);

	int deleteByCodeId(@Param("codeId") String codeId);

	int deleteByCodeIds(@Param("codeIds") List<String> codeIds);

	int deleteByCodeIdAndCodeVal(@Param("codeId") String codeId, @Param("codeVal") String codeVal);
}
