package com.example.springbootapp.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.CalendarEvent;

@Mapper
public interface CalendarEventMapper {

	List<CalendarEvent> findByRange(
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd);

	CalendarEvent findById(@Param("eventId") Long eventId);

	int insert(CalendarEvent event);

	int update(CalendarEvent event);

	int deleteById(@Param("eventId") Long eventId);
}
