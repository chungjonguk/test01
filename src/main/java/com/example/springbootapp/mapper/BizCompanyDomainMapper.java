package com.example.springbootapp.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.BizCompanyDomain;

@Mapper
public interface BizCompanyDomainMapper {

	List<BizCompanyDomain> search(
			@Param("companyId") Long companyId,
			@Param("hostName") String hostName,
			@Param("useYn") String useYn,
			@Param("limit") int limit);

	BizCompanyDomain findById(@Param("domainId") Long domainId);

	BizCompanyDomain findByHostName(@Param("hostName") String hostName);

	int insert(BizCompanyDomain domain);

	int update(BizCompanyDomain domain);

	int deleteById(@Param("domainId") Long domainId);

	int clearPrimaryForCompany(@Param("companyId") Long companyId);
}
