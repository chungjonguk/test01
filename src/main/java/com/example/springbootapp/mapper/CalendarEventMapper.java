package com.example.springbootapp.mapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.CalendarEvent;
/**
 * 캘린더 일정(calendar_event) MyBatis Mapper.
 */
@Mapper
public interface CalendarEventMapper {
	/**
	 * 기간 내 일정 목록을 조회합니다.
	 *
	 * @param rangeStart 조회 시작 일시
	 * @param rangeEnd   조회 종료 일시
	 * @return 일정 목록
	 */
	List<CalendarEvent> findByRange(
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd);
	/**
	 * 일정 ID로 단건 조회합니다.
	 *
	 * @param eventId 일정 ID
	 * @return 일정, 없으면 {@code null}
	 */
	CalendarEvent findById(@Param("eventId") Long eventId);
	/**
	 * 일정을 등록합니다.
	 *
	 * @param event 등록할 일정
	 * @return 반영된 행 수
	 */
	int insert(CalendarEvent event);
	/**
	 * 일정을 수정합니다.
	 *
	 * @param event 수정할 일정
	 * @return 반영된 행 수
	 */
	int update(CalendarEvent event);
	/**
	 * 일정 ID로 단건 삭제합니다.
	 *
	 * @param eventId 일정 ID
	 * @return 삭제된 행 수
	 */
	int deleteById(@Param("eventId") Long eventId);
}
