package com.example.springbootapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * {@code app.random-id.*} — 테이블별 난수 PK 기본 범위·재시도·문자열 길이.
 * <p>개별 테이블 설정은 {@code sys_table_random_id}에서 덮어쓸 수 있습니다.</p>
 */
@Component
@ConfigurationProperties(prefix = "app.random-id")
public class TableRandomIdProperties {

	/** 숫자 ID 최소값 (13자리, AUTO_INCREMENT 1,2,3… 과 구분) */
	private long numericMin = 1_000_000_000_000L;

	/** 숫자 ID 최대값 */
	private long numericMax = 9_999_999_999_999L;

	/** 중복 시 최대 재시도 */
	private int maxRetry = 25;

	/** 문자열 ID 기본 길이 */
	private int defaultStringLength = 16;

	public long getNumericMin() {
		return numericMin;
	}

	public void setNumericMin(long numericMin) {
		this.numericMin = numericMin;
	}

	public long getNumericMax() {
		return numericMax;
	}

	public void setNumericMax(long numericMax) {
		this.numericMax = numericMax;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public int getDefaultStringLength() {
		return defaultStringLength;
	}

	public void setDefaultStringLength(int defaultStringLength) {
		this.defaultStringLength = defaultStringLength;
	}
}
