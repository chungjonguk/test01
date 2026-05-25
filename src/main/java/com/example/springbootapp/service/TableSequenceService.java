package com.example.springbootapp.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.config.TableSequenceCatalog;
import com.example.springbootapp.domain.TableSequence;
import com.example.springbootapp.mapper.TableSequenceMapper;

/**
 * 테이블별 시퀀스 등록·동기화·다음 PK 채번.
 */
@Service
public class TableSequenceService {

	private static final String DEFAULT_ACTOR = "SYSTEM";
	private static final Pattern IDENTIFIER = Pattern.compile("^[a-z][a-z0-9_]*$");

	private final TableSequenceMapper tableSequenceMapper;
	private final JdbcTemplate jdbcTemplate;

	public TableSequenceService(TableSequenceMapper tableSequenceMapper, JdbcTemplate jdbcTemplate) {
		this.tableSequenceMapper = tableSequenceMapper;
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 카탈로그 + DB AUTO_INCREMENT 컬럼 기준으로 시퀀스 행을 등록한다.
	 */
	@Transactional
	public int registerAllFromCatalog() {
		int inserted = 0;
		for (TableSequenceCatalog.Entry entry : TableSequenceCatalog.all()) {
			inserted += registerIfAbsent(entry.tableName(), entry.columnName(), entry.description());
		}
		inserted += registerAutoIncrementColumnsFromDatabase();
		return inserted;
	}

	/**
	 * 각 테이블 MAX(PK) 기준으로 {@code next_val}을 맞춘다 (기존 데이터와 충돌 방지).
	 */
	@Transactional
	public int syncNextValuesFromTables() {
		int synced = 0;
		for (TableSequence seq : tableSequenceMapper.findAllActive()) {
			long maxPk = findMaxPk(seq.getTableName(), seq.getColumnName());
			if (seq.getNextVal() < maxPk) {
				tableSequenceMapper.updateNextVal(seq.getSeqName(), maxPk, DEFAULT_ACTOR);
				synced++;
			}
		}
		return synced;
	}

	/**
	 * 다음 PK 값을 할당한다. (트랜잭션 내 호출 권장)
	 *
	 * @param seqName 시퀀스명 (기본: 테이블명)
	 * @return 할당된 PK
	 */
	@Transactional
	public long nextValue(String seqName) {
		return nextValue(seqName, DEFAULT_ACTOR);
	}

	@Transactional
	public long nextValue(String seqName, String actorId) {
		String name = normalizeSeqName(seqName);
		int updated = tableSequenceMapper.allocateNext(name, actorId != null ? actorId : DEFAULT_ACTOR);
		if (updated == 0) {
			throw new IllegalStateException("시퀀스를 사용할 수 없습니다: " + name);
		}
		Long allocated = tableSequenceMapper.selectLastInsertId();
		if (allocated == null || allocated <= 0) {
			throw new IllegalStateException("시퀀스 채번 실패: " + name);
		}
		return allocated;
	}

	@Transactional
	public long nextValueForTable(String tableName) {
		return nextValue(tableName);
	}

	@Transactional(readOnly = true)
	public TableSequence findBySeqName(String seqName) {
		return tableSequenceMapper.findBySeqName(normalizeSeqName(seqName));
	}

	@Transactional(readOnly = true)
	public List<TableSequence> findAllActive() {
		return tableSequenceMapper.findAllActive();
	}

	@Transactional(readOnly = true)
	public int countAll() {
		return tableSequenceMapper.countAll();
	}

	private int registerIfAbsent(String tableName, String columnName, String description) {
		validateIdentifier(tableName);
		validateIdentifier(columnName);
		TableSequence seq = TableSequenceCatalog.toEntity(
				new TableSequenceCatalog.Entry(tableName, columnName, description));
		return tableSequenceMapper.insertIgnore(seq);
	}

	private int registerAutoIncrementColumnsFromDatabase() {
		List<AutoIncColumn> columns = jdbcTemplate.query(
				"""
						SELECT TABLE_NAME, COLUMN_NAME
						FROM information_schema.COLUMNS
						WHERE TABLE_SCHEMA = DATABASE()
						  AND EXTRA LIKE '%auto_increment%'
						ORDER BY TABLE_NAME
						""",
				(rs, rowNum) -> new AutoIncColumn(
						rs.getString("TABLE_NAME").toLowerCase(),
						rs.getString("COLUMN_NAME").toLowerCase()));
		int inserted = 0;
		for (AutoIncColumn col : columns) {
			inserted += registerIfAbsent(col.tableName(), col.columnName(), col.tableName() + " PK");
		}
		return inserted;
	}

	private long findMaxPk(String tableName, String columnName) {
		validateIdentifier(tableName);
		validateIdentifier(columnName);
		Long max = jdbcTemplate.queryForObject(
				"SELECT COALESCE(MAX(" + columnName + "), 0) FROM " + tableName,
				Long.class);
		return max != null ? max : 0L;
	}

	private static String normalizeSeqName(String seqName) {
		if (seqName == null || seqName.isBlank()) {
			throw new IllegalArgumentException("seqName은 필수입니다.");
		}
		return seqName.trim().toLowerCase();
	}

	private static void validateIdentifier(String name) {
		if (name == null || !IDENTIFIER.matcher(name).matches()) {
			throw new IllegalArgumentException("허용되지 않는 식별자: " + name);
		}
	}

	private record AutoIncColumn(String tableName, String columnName) {
	}
}
