package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.TableSequence;

/**
 * {@code sys_table_sequence} MyBatis 매퍼 — 테이블별 순차 PK 채번.
 */
@Mapper
public interface TableSequenceMapper {

	/** 시퀀스 정의가 없을 때만 등록 */
	int insertIgnore(TableSequence sequence);

	TableSequence findBySeqName(@Param("seqName") String seqName);

	List<TableSequence> findAllActive();

	List<TableSequence> search(
			@Param("seqName") String seqName,
			@Param("tableName") String tableName,
			@Param("useYn") String useYn);

	/** 다음 값을 원자적으로 할당하고 그 값을 반환 ({@code UPDATE ... RETURNING}) */
	Long allocateNext(@Param("seqName") String seqName, @Param("updateId") String updateId);

	int updateNextVal(
			@Param("seqName") String seqName,
			@Param("nextVal") long nextVal,
			@Param("updateId") String updateId);

	int countAll();
}
