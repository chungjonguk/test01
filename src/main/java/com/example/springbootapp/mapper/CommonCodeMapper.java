package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.CommonCode;

@Mapper
public interface CommonCodeMapper {

	List<CommonCode> search(
			@Param("codeId") String codeId,
			@Param("codeNm") String codeNm,
			@Param("useYn") String useYn);

	CommonCode findByCodeId(@Param("codeId") String codeId);

	int insert(CommonCode group);

	int update(CommonCode group);

	int deleteByCodeId(@Param("codeId") String codeId);

	int deleteByCodeIds(@Param("codeIds") List<String> codeIds);
}
