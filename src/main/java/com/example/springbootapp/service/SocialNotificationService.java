package com.example.springbootapp.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.SocialNotification;
import com.example.springbootapp.dto.SocialNotificationFormDto;
import com.example.springbootapp.mapper.SocialNotificationMapper;

import jakarta.servlet.http.HttpSession;

@Service
public class SocialNotificationService {

	private static final int LIST_LIMIT = 100;
	private static final String DEFAULT_USER = "guest";

	private final SocialNotificationMapper socialNotificationMapper;
	private final SessionAuthService sessionAuthService;

	public SocialNotificationService(
			SocialNotificationMapper socialNotificationMapper,
			SessionAuthService sessionAuthService) {
		this.socialNotificationMapper = socialNotificationMapper;
		this.sessionAuthService = sessionAuthService;
	}

	public List<Map<String, Object>> listForApi(HttpSession session, int limit) {
		String userNm = resolveRecipient(session, null);
		int cap = limit > 0 ? Math.min(limit, LIST_LIMIT) : LIST_LIMIT;
		List<SocialNotification> rows = socialNotificationMapper.findByUserNm(userNm, cap);
		return toApiItems(rows);
	}

	public List<Map<String, Object>> searchForGrid(
			String userNm, String senderNm, String readYn, String keyword) {
		List<SocialNotification> rows = socialNotificationMapper.search(
				trimToNull(userNm),
				trimToNull(senderNm),
				normalizeReadYnFilter(readYn),
				trimToNull(keyword),
				LIST_LIMIT);
		return toGridItems(rows);
	}

	@Transactional
	public void delete(Long notificationId) {
		if (notificationId == null) {
			throw new IllegalArgumentException("알림 ID가 필요합니다.");
		}
		if (socialNotificationMapper.findById(notificationId) == null) {
			throw new IllegalArgumentException("알림을 찾을 수 없습니다.");
		}
		socialNotificationMapper.deleteById(notificationId);
	}

	@Transactional
	public Long create(SocialNotificationFormDto dto, HttpSession session) {
		validate(dto);
		String recipient = resolveRecipient(session, dto.getUserNm());
		String sender = trimToNull(dto.getSenderNm());
		if (!StringUtils.hasText(sender)) {
			sender = resolveActor(session);
		}
		String section = normalizeSection(dto.getSectionCd());
		String timeIcon = trimToNull(dto.getTimeIcon());
		if (!StringUtils.hasText(timeIcon)) {
			timeIcon = "📢";
		}

		SocialNotification row = new SocialNotification();
		row.setUserNm(recipient);
		row.setSenderNm(sender);
		row.setMessage(buildBodyHtml(sender, dto.getMessage().trim()));
		row.setSectionCd(section);
		row.setTimeIcon(timeIcon);
		row.setReadYn("N");
		row.setNotifiedDt(LocalDateTime.now());
		socialNotificationMapper.insert(row);
		return row.getNotificationId();
	}

	@Transactional
	public void markRead(Long notificationId) {
		if (notificationId == null) {
			throw new IllegalArgumentException("알림 ID가 필요합니다.");
		}
		if (socialNotificationMapper.findById(notificationId) == null) {
			throw new IllegalArgumentException("알림을 찾을 수 없습니다.");
		}
		socialNotificationMapper.updateReadYn(notificationId, "Y");
	}

	@Transactional
	public int markAllRead(HttpSession session) {
		String userNm = resolveRecipient(session, null);
		return socialNotificationMapper.markAllRead(userNm);
	}

	private void validate(SocialNotificationFormDto dto) {
		if (dto == null || !StringUtils.hasText(dto.getMessage())) {
			throw new IllegalArgumentException("알림 내용을 입력하세요.");
		}
		if (dto.getMessage().trim().length() > 500) {
			throw new IllegalArgumentException("알림 내용은 500자 이하여야 합니다.");
		}
	}

	private String resolveRecipient(HttpSession session, String requested) {
		if (StringUtils.hasText(requested)) {
			return requested.trim();
		}
		String login = sessionAuthService.getLoginUserId(session);
		return StringUtils.hasText(login) ? login.trim() : DEFAULT_USER;
	}

	private String resolveActor(HttpSession session) {
		String login = sessionAuthService.getLoginUserId(session);
		return StringUtils.hasText(login) ? login.trim() : "시스템";
	}

	private String normalizeSection(String sectionCd) {
		if (!StringUtils.hasText(sectionCd)) {
			return "NEW";
		}
		String s = sectionCd.trim().toUpperCase(Locale.ROOT);
		if ("EARLIER".equals(s) || "NEW".equals(s)) {
			return s;
		}
		return "NEW";
	}

	private String buildBodyHtml(String senderNm, String plainMessage) {
		String safeSender = escapeHtml(senderNm != null ? senderNm : "알림");
		String safeMessage = escapeHtml(plainMessage);
		return "<strong>" + safeSender + "</strong> " + safeMessage;
	}

	private String escapeHtml(String value) {
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private List<Map<String, Object>> toApiItems(List<SocialNotification> rows) {
		List<Map<String, Object>> items = new ArrayList<>();
		for (SocialNotification row : rows) {
			items.add(toApiItem(row));
		}
		return items;
	}

	private List<Map<String, Object>> toGridItems(List<SocialNotification> rows) {
		List<Map<String, Object>> items = new ArrayList<>();
		for (SocialNotification row : rows) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("notificationId", row.getNotificationId());
			item.put("userNm", row.getUserNm());
			item.put("senderNm", row.getSenderNm());
			item.put("sectionCd", row.getSectionCd());
			item.put("timeIcon", StringUtils.hasText(row.getTimeIcon()) ? row.getTimeIcon() : "📢");
			item.put("avatarType", "emoji");
			item.put("message", row.getMessage());
			item.put("messageText", stripHtml(row.getMessage()));
			item.put("readYn", row.getReadYn());
			item.put("unread", !"Y".equalsIgnoreCase(row.getReadYn()));
			item.put("notifiedDt", row.getNotifiedDt());
			item.put("notifiedDtLabel", formatTimeLabel(row.getNotifiedDt()));
			items.add(item);
		}
		return items;
	}

	private String normalizeReadYnFilter(String readYn) {
		if (!StringUtils.hasText(readYn)) {
			return null;
		}
		String yn = readYn.trim().toUpperCase(Locale.ROOT);
		if ("Y".equals(yn) || "N".equals(yn)) {
			return yn;
		}
		return null;
	}

	private String stripHtml(String html) {
		if (!StringUtils.hasText(html)) {
			return "";
		}
		return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
	}

	private Map<String, Object> toApiItem(SocialNotification row) {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("notificationId", row.getNotificationId());
		item.put("id", "db-" + row.getNotificationId());
		item.put("userNm", row.getUserNm());
		item.put("senderNm", row.getSenderNm());
		item.put("message", row.getMessage());
		item.put("body", row.getMessage());
		item.put("sectionCd", row.getSectionCd());
		item.put("section", StringUtils.hasText(row.getSectionCd()) ? row.getSectionCd() : "NEW");
		item.put("timeIcon", row.getTimeIcon());
		item.put("readYn", row.getReadYn());
		item.put("unread", !"Y".equalsIgnoreCase(row.getReadYn()));
		item.put("notifiedDt", row.getNotifiedDt());
		item.put("timeLabel", formatTimeLabel(row.getNotifiedDt()));
		item.put("avatarType", "emoji");
		if (!StringUtils.hasText(row.getTimeIcon())) {
			item.put("timeIcon", "📢");
		}
		return item;
	}

	private String formatTimeLabel(LocalDateTime dt) {
		if (dt == null) {
			return "";
		}
		LocalDateTime now = LocalDateTime.now();
		Duration diff = Duration.between(dt, now);
		long minutes = diff.toMinutes();
		if (minutes < 1) {
			return "방금";
		}
		if (minutes < 60) {
			return minutes + "분";
		}
		long hours = diff.toHours();
		if (hours < 24) {
			return hours + "시간";
		}
		long days = diff.toDays();
		if (days < 7) {
			return days + "일";
		}
		return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
}
