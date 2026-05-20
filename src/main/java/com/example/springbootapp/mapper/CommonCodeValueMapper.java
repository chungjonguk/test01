package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.CommonCodeValue;

@Mapper
public interface CommonCodeValueMapper {

	List<CommonCodeValue> findByCodeId(@Param("codeId") String codeId);

	List<CommonCodeValue> findByCodeIds(@Param("codeIds") List<String> codeIds);
}
