package com.example.springbootapp.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.CalendarEvent;
import com.example.springbootapp.dto.CalendarEventFormDto;
import com.example.springbootapp.mapper.CalendarEventMapper;

import jakarta.servlet.http.HttpSession;

@Service
@Transactional(readOnly = true)
public class CalendarEventService {

	private static final DateTimeFormatter[] PARSE_FORMATS = {
			DateTimeFormatter.ISO_LOCAL_DATE_TIME,
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
	};

	private final CalendarEventMapper calendarEventMapper;
	private final SessionAuthService sessionAuthService;

	public CalendarEventService(CalendarEventMapper calendarEventMapper, SessionAuthService sessionAuthService) {
		this.calendarEventMapper = calendarEventMapper;
		this.sessionAuthService = sessionAuthService;
	}

	public List<CalendarEvent> findByRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		if (rangeStart == null || rangeEnd == null) {
			throw new IllegalArgumentException("조회 기간(start, end)이 필요합니다.");
		}
		if (!rangeEnd.isAfter(rangeStart)) {
			throw new IllegalArgumentException("종료 시각은 시작 시각보다 이후여야 합니다.");
		}
		return calendarEventMapper.findByRange(rangeStart, rangeEnd);
	}

	public CalendarEvent findById(Long eventId) {
		if (eventId == null) {
			return null;
		}
		return calendarEventMapper.findById(eventId);
	}

	@Transactional
	public Long save(CalendarEventFormDto dto, HttpSession session) {
		validate(dto);
		String actor = resolveActor(session);
		CalendarEvent event = toEntity(dto);
		event.setRegId(actor);
		event.setUpdateId(actor);

		if (dto.getEventId() == null) {
			calendarEventMapper.insert(event);
			return event.getEventId();
		}
		CalendarEvent existing = calendarEventMapper.findById(dto.getEventId());
		if (existing == null) {
			throw new IllegalArgumentException("일정을 찾을 수 없습니다.");
		}
		event.setEventId(dto.getEventId());
		calendarEventMapper.update(event);
		return event.getEventId();
	}

	@Transactional
	public void delete(Long eventId) {
		if (eventId == null) {
			throw new IllegalArgumentException("일정 ID가 필요합니다.");
		}
		if (calendarEventMapper.findById(eventId) == null) {
			throw new IllegalArgumentException("일정을 찾을 수 없습니다.");
		}
		calendarEventMapper.deleteById(eventId);
	}

	private void validate(CalendarEventFormDto dto) {
		if (dto == null) {
			throw new IllegalArgumentException("입력값이 없습니다.");
		}
		if (isBlank(dto.getTitle())) {
			throw new IllegalArgumentException("제목을 입력해 주세요.");
		}
		if (isBlank(dto.getStartDt())) {
			throw new IllegalArgumentException("시작 일시를 입력해 주세요.");
		}
	}

	private CalendarEvent toEntity(CalendarEventFormDto dto) {
		CalendarEvent e = new CalendarEvent();
		e.setTitle(dto.getTitle().trim());
		e.setCategoryCd(trimToNull(dto.getCategoryCd()));
		e.setLabelCd(trimToNull(dto.getLabelCd()));
		e.setStartDt(parseDateTime(dto.getStartDt()));
		boolean allDay = Boolean.TRUE.equals(dto.getAllDay());
		e.setAllDayYn(allDay ? "Y" : "N");
		if (allDay) {
			e.setEndDt(parseDateTime(dto.getEndDt() != null && !dto.getEndDt().isBlank() ? dto.getEndDt() : dto.getStartDt()));
		} else {
			e.setEndDt(isBlank(dto.getEndDt()) ? null : parseDateTime(dto.getEndDt()));
		}
		e.setLocation(trimToNull(dto.getLocation()));
		e.setDescription(trimToNull(dto.getDescription()));
		return e;
	}

	public static LocalDateTime parseDateTime(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String normalized = value.trim().replace(' ', 'T');
		for (DateTimeFormatter formatter : PARSE_FORMATS) {
			try {
				return LocalDateTime.parse(normalized, formatter);
			} catch (DateTimeParseException ignored) {
				// try next
			}
		}
		try {
			return LocalDateTime.parse(normalized);
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다: " + value);
		}
	}

	private String resolveActor(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		return userId != null && !userId.isBlank() ? userId : "SYSTEM";
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		return t.isEmpty() ? null : t;
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
