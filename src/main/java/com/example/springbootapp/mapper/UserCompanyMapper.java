package com.example.springbootapp.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserCompanyMapper {

	List<Long> findCompanyIdsByUserId(@Param("userId") String userId);

	int deleteByUserId(@Param("userId") String userId);

	int insert(
			@Param("userId") String userId,
			@Param("companyId") Long companyId,
			@Param("regId") String regId,
			@Param("updateId") String updateId);
}
