package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.TableRandomId;

/**
 * {@code sys_table_random_id} MyBatis 매퍼 — 테이블별 난수 PK 설정 조회·등록.
 */
@Mapper
public interface TableRandomIdMapper {

	int insertIgnore(TableRandomId config);

	TableRandomId findByConfigName(@Param("configName") String configName);

	List<TableRandomId> findAllActive();

	int countAll();
}
