package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.ScreenList;

@Mapper
public interface ScreenListMapper {

	List<ScreenList> findAllActive();

	List<ScreenList> findForAdmin(
			@Param("screenId") String screenId,
			@Param("screenNm") String screenNm,
			@Param("uriPath") String uriPath,
			@Param("useYn") String useYn);

	ScreenList findByScreenId(@Param("screenId") String screenId);

	ScreenList findByUriPath(@Param("uriPath") String uriPath);

	int countAll();

	int insert(ScreenList screen);

	int update(ScreenList screen);
}
