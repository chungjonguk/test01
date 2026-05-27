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

	/** 사용 중인 도메인·업체에 매핑된 Host (대표 도메인 우선) */
	BizCompanyDomain findActiveByHostName(@Param("hostName") String hostName);

	int insert(BizCompanyDomain domain);

	int update(BizCompanyDomain domain);

	int deleteById(@Param("domainId") Long domainId);

	int clearPrimaryForCompany(@Param("companyId") Long companyId);
}
