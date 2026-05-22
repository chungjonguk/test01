package com.example.springbootapp.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.UserAccessLog;

@Mapper
public interface UserAccessLogMapper {

	int insert(UserAccessLog log);

	List<UserAccessLog> findByUserId(
			@Param("userId") String userId,
			@Param("limit") int limit);

	List<UserAccessLog> findByRange(
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd,
			@Param("limit") int limit);

	List<UserAccessLog> search(
			@Param("userId") String userId,
			@Param("accessTypeCd") String accessTypeCd,
			@Param("loginTypeCd") String loginTypeCd,
			@Param("successYn") String successYn,
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd,
			@Param("limit") int limit);
}
