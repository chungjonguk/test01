package com.example.springbootapp.mapper;

import com.example.springbootapp.domain.BizCompanyPageImage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizCompanyPageImageMapper {

	List<BizCompanyPageImage> listByCompanyId(@Param("companyId") Long companyId);

	BizCompanyPageImage findByCompanyAndPageCd(
			@Param("companyId") Long companyId,
			@Param("pageCd") String pageCd);

	BizCompanyPageImage findById(@Param("imageId") Long imageId);

	int insert(BizCompanyPageImage row);

	int update(BizCompanyPageImage row);

	int deleteById(@Param("imageId") Long imageId);
}
