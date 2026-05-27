package com.example.springbootapp.mapper;

import com.example.springbootapp.domain.CompanyCustomerMenu;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyCustomerMenuMapper {

	List<CompanyCustomerMenu> listByCompanyId(@Param("companyId") Long companyId);

	int deleteByCompanyId(@Param("companyId") Long companyId);

	int insert(CompanyCustomerMenu row);
}
