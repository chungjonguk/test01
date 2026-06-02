package com.example.springbootapp.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.config.TableRandomIdProperties;
import com.example.springbootapp.config.TableSequenceCatalog;
import com.example.springbootapp.domain.TableRandomId;
import com.example.springbootapp.mapper.TableRandomIdMapper;
import com.example.springbootapp.util.RandomIdGenerator;

/**
 * 테이블별 난수 PK 등록·중복 검사 후 채번.
 */
@Service
public class TableRandomIdService {

	private static final String DEFAULT_ACTOR = "SYSTEM";
	private static final Pattern IDENTIFIER = Pattern.compile("^[a-z][a-z0-9_]*$");

	private final TableRandomIdMapper tableRandomIdMapper;
	private final JdbcTemplate jdbcTemplate;
	private final TableRandomIdProperties properties;

	public TableRandomIdService(
			TableRandomIdMapper tableRandomIdMapper,
			JdbcTemplate jdbcTemplate,
			TableRandomIdProperties properties) {
		this.tableRandomIdMapper = tableRandomIdMapper;
		this.jdbcTemplate = jdbcTemplate;
		this.properties = properties;
	}

	/**
	 * 카탈로그 + DB AUTO_INCREMENT 컬럼 기준으로 난수 ID 설정을 등록한다.
	 */
	@Transactional
	public int registerAllFromCatalog() {
		int inserted = 0;
		for (TableSequenceCatalog.Entry entry : TableSequenceCatalog.all()) {
			inserted += registerNumericIfAbsent(entry.tableName(), entry.columnName(), entry.description());
		}
		inserted += registerAutoIncrementColumnsFromDatabase();
		inserted += registerStringIfAbsent("user", "id", properties.getDefaultStringLength(), "회원 ID");
		return inserted;
	}

	/**
	 * BIGINT PK용 난수 ID를 발급한다 (테이블에 동일 값 없을 때까지 재시도).
	 */
	@Transactional
	public long nextRandomLong(String tableName) {
		TableRandomId config = requireActiveConfig(tableName);
		if (!config.isNumericType()) {
			throw new IllegalStateException("숫자형 난수 ID가 아닙니다: " + tableName);
		}
		int maxRetry = effectiveMaxRetry(config);
		for (int attempt = 0; attempt < maxRetry; attempt++) {
			long candidate = RandomIdGenerator.nextLongInRange(config.getMinVal(), config.getMaxVal());
			if (!existsNumeric(config, candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException(
				"난수 ID 채번 실패(중복 초과): " + config.getTableName() + ", 재시도=" + maxRetry);
	}

	/**
	 * VARCHAR PK용 영숫자 난수 ID를 발급한다.
	 */
	@Transactional
	public String nextRandomString(String tableName) {
		TableRandomId config = requireActiveConfig(tableName);
		if (!config.isStringType()) {
			throw new IllegalStateException("문자열형 난수 ID가 아닙니다: " + tableName);
		}
		int length = config.getStringLength() != null && config.getStringLength() > 0
				? config.getStringLength()
				: properties.getDefaultStringLength();
		int maxRetry = effectiveMaxRetry(config);
		for (int attempt = 0; attempt < maxRetry; attempt++) {
			String candidate = RandomIdGenerator.nextAlphanumeric(length);
			if (!existsString(config, candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException(
				"난수 ID 채번 실패(중복 초과): " + config.getTableName() + ", 재시도=" + maxRetry);
	}

	@Transactional
	public long nextRandomLongForTable(String tableName) {
		return nextRandomLong(tableName);
	}

	@Transactional(readOnly = true)
	public TableRandomId findByConfigName(String configName) {
		return tableRandomIdMapper.findByConfigName(normalizeConfigName(configName));
	}

	@Transactional(readOnly = true)
	public List<TableRandomId> findAllActive() {
		return tableRandomIdMapper.findAllActive();
	}

	@Transactional(readOnly = true)
	public int countAll() {
		return tableRandomIdMapper.countAll();
	}

	@Transactional
	public int registerStringIfAbsent(String tableName, String columnName, int length, String description) {
		validateIdentifier(tableName);
		validateIdentifier(columnName);
		TableRandomId config = new TableRandomId();
		config.setConfigName(tableName);
		config.setTableName(tableName);
		config.setColumnName(columnName);
		config.setIdTypeCd("S");
		config.setMinVal(0L);
		config.setMaxVal(0L);
		config.setStringLength(length);
		config.setMaxRetry(properties.getMaxRetry());
		config.setDescription(description);
		config.setUseYn("Y");
		config.setRegId(DEFAULT_ACTOR);
		config.setUpdateId(DEFAULT_ACTOR);
		return tableRandomIdMapper.insertIgnore(config);
	}

	private int registerNumericIfAbsent(String tableName, String columnName, String description) {
		validateIdentifier(tableName);
		validateIdentifier(columnName);
		TableRandomId config = new TableRandomId();
		config.setConfigName(tableName);
		config.setTableName(tableName);
		config.setColumnName(columnName);
		config.setIdTypeCd("N");
		config.setMinVal(properties.getNumericMin());
		config.setMaxVal(properties.getNumericMax());
		config.setMaxRetry(properties.getMaxRetry());
		config.setDescription(description);
		config.setUseYn("Y");
		config.setRegId(DEFAULT_ACTOR);
		config.setUpdateId(DEFAULT_ACTOR);
		return tableRandomIdMapper.insertIgnore(config);
	}

	private int registerAutoIncrementColumnsFromDatabase() {
		List<AutoIncColumn> columns = jdbcTemplate.query(
				"""
						SELECT table_name, column_name
						FROM information_schema.columns
						WHERE table_schema = current_schema()
						  AND (is_identity = 'YES' OR column_default LIKE 'nextval(%')
						ORDER BY table_name
						""",
				(rs, rowNum) -> new AutoIncColumn(
						rs.getString("table_name").toLowerCase(),
						rs.getString("column_name").toLowerCase()));
		int inserted = 0;
		for (AutoIncColumn col : columns) {
			inserted += registerNumericIfAbsent(col.tableName(), col.columnName(), col.tableName() + " PK 난수");
		}
		return inserted;
	}

	private TableRandomId requireActiveConfig(String tableName) {
		TableRandomId config = tableRandomIdMapper.findByConfigName(normalizeConfigName(tableName));
		if (config == null || !"Y".equalsIgnoreCase(config.getUseYn())) {
			throw new IllegalStateException("난수 ID 설정이 없습니다: " + tableName);
		}
		validateIdentifier(config.getTableName());
		validateIdentifier(config.getColumnName());
		return config;
	}

	private boolean existsNumeric(TableRandomId config, long candidate) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM " + config.getTableName()
						+ " WHERE " + config.getColumnName() + " = ?",
				Integer.class,
				candidate);
		return count != null && count > 0;
	}

	private boolean existsString(TableRandomId config, String candidate) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM " + config.getTableName()
						+ " WHERE " + config.getColumnName() + " = ?",
				Integer.class,
				candidate);
		return count != null && count > 0;
	}

	private int effectiveMaxRetry(TableRandomId config) {
		return config.getMaxRetry() > 0 ? config.getMaxRetry() : properties.getMaxRetry();
	}

	private static String normalizeConfigName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("configName은 필수입니다.");
		}
		return name.trim().toLowerCase();
	}

	private static void validateIdentifier(String name) {
		if (name == null || !IDENTIFIER.matcher(name).matches()) {
			throw new IllegalArgumentException("허용되지 않는 식별자: " + name);
		}
	}

	private record AutoIncColumn(String tableName, String columnName) {
	}
}
