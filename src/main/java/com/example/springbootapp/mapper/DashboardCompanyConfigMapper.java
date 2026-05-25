package com.example.springbootapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.DashboardCompanyConfig;

@Mapper
public interface DashboardCompanyConfigMapper {

	DashboardCompanyConfig findByCompanyId(@Param("companyId") Long companyId);

	int insert(DashboardCompanyConfig config);

	int update(DashboardCompanyConfig config);
}
