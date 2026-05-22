package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.ScreenTableMap;

@Mapper
public interface ScreenTableMapMapper {

	ScreenTableMap findByUriPath(@Param("uriPath") String uriPath);

	List<ScreenTableMap> findAll();

	int upsert(ScreenTableMap row);
}
