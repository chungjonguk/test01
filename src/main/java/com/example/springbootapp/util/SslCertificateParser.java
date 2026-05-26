package com.example.springbootapp.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * PEM/DER X.509 SSL 인증서 파일에서 유효기간·주체 정보를 추출합니다.
 */
public final class SslCertificateParser {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	private SslCertificateParser() {
	}

	public record ParsedSslCertificate(
			LocalDateTime notBefore,
			LocalDateTime notAfter,
			String subjectDn,
			String issuerDn,
			int certificateCount) {
	}

	public static ParsedSslCertificate parse(InputStream input) throws IOException {
		return parse(input, null);
	}

	public static ParsedSslCertificate parse(InputStream input, String hostName) throws IOException {
		try (InputStream in = new BufferedInputStream(input)) {
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			Collection<? extends Certificate> certs = factory.generateCertificates(in);
			if (certs == null || certs.isEmpty()) {
				throw new IllegalArgumentException("인증서 파일에서 X.509 인증서를 찾을 수 없습니다.");
			}
			X509Certificate selected = selectCertificate(certs, hostName);
			return toParsed(selected, certs.size());
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalArgumentException("SSL 인증서 파일을 읽을 수 없습니다. (.pem, .crt, .cer 형식 확인)", ex);
		}
	}

	private static X509Certificate selectCertificate(Collection<? extends Certificate> certs, String hostName) {
		List<X509Certificate> x509 = new ArrayList<>();
		for (Certificate cert : certs) {
			if (cert instanceof X509Certificate x) {
				x509.add(x);
			}
		}
		if (x509.isEmpty()) {
			throw new IllegalArgumentException("X.509 인증서가 없습니다.");
		}
		String normalizedHost = normalizeHost(hostName);
		if (normalizedHost != null) {
			for (X509Certificate cert : x509) {
				if (matchesHost(cert, normalizedHost)) {
					return cert;
				}
			}
		}
		return x509.stream()
				.filter(cert -> !isCaCertificate(cert))
				.max(Comparator.comparing(SslCertificateParser::toNotAfterInstant))
				.orElseGet(() -> x509.stream()
						.max(Comparator.comparing(SslCertificateParser::toNotAfterInstant))
						.orElse(x509.get(0)));
	}

	private static boolean isCaCertificate(X509Certificate cert) {
		try {
			if (cert.getBasicConstraints() >= 0) {
				return true;
			}
		} catch (Exception ignored) {
			// ignore
		}
		String subject = cert.getSubjectX500Principal().getName();
		String issuer = cert.getIssuerX500Principal().getName();
		return subject != null && subject.equals(issuer);
	}

	private static boolean matchesHost(X509Certificate cert, String hostName) {
		String subject = cert.getSubjectX500Principal().getName();
		if (subject == null) {
			return false;
		}
		String lower = subject.toLowerCase(Locale.ROOT);
		if (lower.contains("cn=" + hostName)) {
			return true;
		}
		try {
			var sans = cert.getSubjectAlternativeNames();
			if (sans == null) {
				return false;
			}
			for (var entry : sans) {
				if (entry == null || entry.size() < 2 || entry.get(1) == null) {
					continue;
				}
				String value = entry.get(1).toString().toLowerCase(Locale.ROOT);
				if (value.startsWith("*.")) {
					String suffix = value.substring(2);
					if (hostName.endsWith(suffix) && hostName.length() > suffix.length()) {
						return true;
					}
				} else if (hostName.equals(value)) {
					return true;
				}
			}
		} catch (Exception ignored) {
			// ignore
		}
		return false;
	}

	private static String normalizeHost(String hostName) {
		if (hostName == null || hostName.isBlank()) {
			return null;
		}
		String host = hostName.trim().toLowerCase(Locale.ROOT);
		if (host.startsWith("https://")) {
			host = host.substring(8);
		} else if (host.startsWith("http://")) {
			host = host.substring(7);
		}
		int slash = host.indexOf('/');
		if (slash >= 0) {
			host = host.substring(0, slash);
		}
		int colon = host.indexOf(':');
		if (colon >= 0) {
			host = host.substring(0, colon);
		}
		if (host.startsWith("www.")) {
			host = host.substring(4);
		}
		return host.isBlank() ? null : host;
	}

	private static ParsedSslCertificate toParsed(X509Certificate cert, int count) {
		return new ParsedSslCertificate(
				toLocalDateTime(cert.getNotBefore()),
				toLocalDateTime(cert.getNotAfter()),
				cert.getSubjectX500Principal().getName(),
				cert.getIssuerX500Principal().getName(),
				count);
	}

	private static LocalDateTime toLocalDateTime(java.util.Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZONE);
	}

	private static Instant toNotAfterInstant(X509Certificate cert) {
		return cert.getNotAfter().toInstant();
	}
}
