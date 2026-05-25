package com.example.springbootapp.util;

import java.security.SecureRandom;

/**
 * PK용 난수 생성 유틸 ({@link java.security.SecureRandom}).
 * <p>{@link com.example.springbootapp.service.TableRandomIdService}에서 사용합니다.</p>
 */
public final class RandomIdGenerator {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final char[] ALPHANUMERIC =
			"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	private RandomIdGenerator() {
	}

	/** [minInclusive, maxInclusive] 구간의 균등 분포 long 난수 */
	public static long nextLongInRange(long minInclusive, long maxInclusive) {
		if (minInclusive > maxInclusive) {
			throw new IllegalArgumentException("min > max");
		}
		if (minInclusive == maxInclusive) {
			return minInclusive;
		}
		long bound = maxInclusive - minInclusive + 1;
		long raw = Math.abs(RANDOM.nextLong());
		return minInclusive + (raw % bound);
	}

	/** 0-9, A-Z, a-z 조합 문자열 난수 */
	public static String nextAlphanumeric(int length) {
		if (length < 1 || length > 64) {
			throw new IllegalArgumentException("length는 1~64 이어야 합니다.");
		}
		char[] buf = new char[length];
		for (int i = 0; i < length; i++) {
			buf[i] = ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)];
		}
		return new String(buf);
	}
}
