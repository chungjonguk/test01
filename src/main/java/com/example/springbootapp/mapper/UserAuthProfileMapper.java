package com.example.springbootapp.mapper;

import com.example.springbootapp.domain.UserAuthProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAuthProfileMapper {

	UserAuthProfile findByUserId(@Param("userId") String userId);

	int upsert(UserAuthProfile profile);
}
