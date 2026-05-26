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
import com.example.springbootapp.util.AppDateTimeFormats;
import jakarta.servlet.http.HttpSession;
/**
 * 캘린더 일정 조회·등록·수정·삭제 및 날짜 문자열 파싱을 처리하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class CalendarEventService {
	private static final DateTimeFormatter[] PARSE_FORMATS = {
			AppDateTimeFormats.DATETIME_FORMATTER,
			AppDateTimeFormats.DATE_FORMATTER,
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
	/**
	 * 기간 내 일정 목록을 조회한다.
	 *
	 * @param rangeStart 조회 시작 일시
	 * @param rangeEnd   조회 종료 일시 (시작보다 이후)
	 * @return 해당 기간의 일정 목록
	 */
	public List<CalendarEvent> findByRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		if (rangeStart == null || rangeEnd == null) {
			throw new IllegalArgumentException("조회 기간(start, end)이 필요합니다.");
		}
		if (!rangeEnd.isAfter(rangeStart)) {
			throw new IllegalArgumentException("종료 시각은 시작 시각보다 이후여야 합니다.");
		}
		return calendarEventMapper.findByRange(rangeStart, rangeEnd);
	}
	/**
	 * 일정 ID로 단건을 조회한다.
	 *
	 * @param eventId 일정 ID
	 * @return 일정 엔티티, ID가 null이거나 없으면 null
	 */
	public CalendarEvent findById(Long eventId) {
		if (eventId == null) {
			return null;
		}
		return calendarEventMapper.findById(eventId);
	}
	/**
	 * 일정을 신규 등록하거나 기존 일정을 수정한다.
	 *
	 * @param dto     일정 입력 폼
	 * @param session HTTP 세션 (등록·수정자 ID 추출용)
	 * @return 저장된 일정 ID
	 */
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
	/**
	 * 일정을 삭제한다.
	 *
	 * @param eventId 삭제할 일정 ID
	 */
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
	/**
	 * 다양한 형식의 날짜·시간 문자열을 {@link LocalDateTime}으로 파싱한다.
	 *
	 * @param value 날짜·시간 문자열
	 * @return 파싱된 일시, 빈 값이면 null
	 */
	public static LocalDateTime parseDateTime(String value) {
		try {
			return AppDateTimeFormats.parseDateTime(value);
		} catch (Exception ex) {
			String normalized = value != null ? value.trim().replace(' ', 'T') : "";
			for (DateTimeFormatter formatter : PARSE_FORMATS) {
				try {
					return LocalDateTime.parse(normalized, formatter);
				} catch (DateTimeParseException ignored) {
					// try next
				}
			}
			throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다 (yyyy-MM-dd HH:mm:ss): " + value);
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
