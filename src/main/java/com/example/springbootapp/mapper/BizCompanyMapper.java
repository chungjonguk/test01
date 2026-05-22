package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.BizCompany;

@Mapper
public interface BizCompanyMapper {

	List<BizCompany> search(
			@Param("companyNm") String companyNm,
			@Param("bizNo") String bizNo,
			@Param("statusCd") String statusCd,
			@Param("useYn") String useYn,
			@Param("limit") int limit);

	BizCompany findById(@Param("companyId") Long companyId);

	int insert(BizCompany company);

	int update(BizCompany company);

	int deleteById(@Param("companyId") Long companyId);
}
