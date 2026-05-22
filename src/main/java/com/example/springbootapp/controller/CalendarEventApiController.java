package com.example.springbootapp.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootapp.domain.CalendarEvent;
import com.example.springbootapp.dto.CalendarEventFormDto;
import com.example.springbootapp.service.CalendarEventService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/calendar/events")
public class CalendarEventApiController {

	private final CalendarEventService calendarEventService;

	public CalendarEventApiController(CalendarEventService calendarEventService) {
		this.calendarEventService = calendarEventService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam String start,
			@RequestParam String end) {
		LocalDateTime rangeStart = CalendarEventService.parseDateTime(start);
		LocalDateTime rangeEnd = CalendarEventService.parseDateTime(end);
		List<Map<String, Object>> events = calendarEventService.findByRange(rangeStart, rangeEnd).stream()
				.map(this::toFullCalendarDto)
				.collect(Collectors.toList());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("events", events);
		body.put("count", events.size());
		return ResponseEntity.ok(body);
	}

	@GetMapping("/{eventId}")
	public ResponseEntity<Map<String, Object>> get(@PathVariable Long eventId) {
		CalendarEvent event = calendarEventService.findById(eventId);
		if (event == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(toDetailDto(event));
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody CalendarEventFormDto dto, HttpSession session) {
		try {
			Long id = calendarEventService.save(dto, session);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("eventId", id);
			body.put("message", "일정이 등록되었습니다.");
			return ResponseEntity.status(HttpStatus.CREATED).body(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@PutMapping("/{eventId}")
	public ResponseEntity<Map<String, Object>> update(
			@PathVariable Long eventId,
			@RequestBody CalendarEventFormDto dto,
			HttpSession session) {
		dto.setEventId(eventId);
		try {
			Long id = calendarEventService.save(dto, session);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("success", true);
			body.put("eventId", id);
			body.put("message", "일정이 수정되었습니다.");
			return ResponseEntity.ok(body);
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	@DeleteMapping("/{eventId}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long eventId) {
		try {
			calendarEventService.delete(eventId);
			return ResponseEntity.ok(Map.of("success", true, "message", "일정이 삭제되었습니다."));
		} catch (IllegalArgumentException ex) {
			return badRequest(ex.getMessage());
		}
	}

	private Map<String, Object> toFullCalendarDto(CalendarEvent e) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("id", String.valueOf(e.getEventId()));
		row.put("title", e.getTitle());
		row.put("start", formatFcDate(e.getStartDt(), "Y".equalsIgnoreCase(e.getAllDayYn())));
		if (e.getEndDt() != null) {
			row.put("end", formatFcDate(e.getEndDt(), "Y".equalsIgnoreCase(e.getAllDayYn())));
		}
		row.put("allDay", "Y".equalsIgnoreCase(e.getAllDayYn()));
		String label = e.getLabelCd();
		if (label != null && !label.isBlank()) {
			row.put("className", "bg-soft-" + label.trim());
		}
		Map<String, Object> ext = new LinkedHashMap<>();
		ext.put("eventId", e.getEventId());
		ext.put("description", e.getDescription());
		ext.put("location", e.getLocation());
		ext.put("categoryCd", e.getCategoryCd());
		ext.put("labelCd", e.getLabelCd());
		row.put("extendedProps", ext);
		return row;
	}

	private Map<String, Object> toDetailDto(CalendarEvent e) {
		Map<String, Object> row = toFullCalendarDto(e);
		row.put("eventId", e.getEventId());
		row.put("categoryCd", e.getCategoryCd());
		row.put("labelCd", e.getLabelCd());
		row.put("description", e.getDescription());
		row.put("location", e.getLocation());
		row.put("startDt", e.getStartDt() != null ? e.getStartDt().toString() : null);
		row.put("endDt", e.getEndDt() != null ? e.getEndDt().toString() : null);
		row.put("allDayYn", e.getAllDayYn());
		return row;
	}

	private static String formatFcDate(LocalDateTime dt, boolean allDay) {
		if (dt == null) {
			return null;
		}
		if (allDay) {
			return dt.toLocalDate().toString();
		}
		return dt.toString().replace(' ', 'T');
	}

	private ResponseEntity<Map<String, Object>> badRequest(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("message", message);
		return ResponseEntity.badRequest().body(body);
	}
}
