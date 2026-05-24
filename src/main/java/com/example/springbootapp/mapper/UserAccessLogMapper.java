package com.example.springbootapp.mapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.UserAccessLog;
/**
 * 사용자 접속·로그인 이력(user_access_log) MyBatis Mapper.
 */
@Mapper
public interface UserAccessLogMapper {
	/**
	 * 접속 로그를 등록합니다.
	 *
	 * @param log 등록할 접속 로그
	 * @return 반영된 행 수
	 */
	int insert(UserAccessLog log);
	/**
	 * 사용자 ID별 최근 접속 로그를 조회합니다.
	 *
	 * @param userId 사용자 ID
	 * @param limit  최대 조회 건수
	 * @return 접속 로그 목록
	 */
	List<UserAccessLog> findByUserId(
			@Param("userId") String userId,
			@Param("limit") int limit);
	/**
	 * 기간 내 접속 로그를 조회합니다.
	 *
	 * @param rangeStart 조회 시작 일시
	 * @param rangeEnd   조회 종료 일시
	 * @param limit      최대 조회 건수
	 * @return 접속 로그 목록
	 */
	List<UserAccessLog> findByRange(
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd,
			@Param("limit") int limit);
	/**
	 * 다중 조건으로 접속 로그를 검색합니다.
	 *
	 * @param userId        사용자 ID (nullable)
	 * @param clientIp      클라이언트 IP (nullable)
	 * @param accessTypeCd  접속 유형 코드 (nullable)
	 * @param loginTypeCd   로그인 유형 코드 (nullable)
	 * @param successYn     성공 여부 (nullable)
	 * @param rangeStart    조회 시작 일시 (nullable)
	 * @param rangeEnd      조회 종료 일시 (nullable)
	 * @param limit         최대 조회 건수
	 * @return 접속 로그 목록
	 */
	List<UserAccessLog> search(
			@Param("userId") String userId,
			@Param("clientIp") String clientIp,
			@Param("accessTypeCd") String accessTypeCd,
			@Param("loginTypeCd") String loginTypeCd,
			@Param("successYn") String successYn,
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd,
			@Param("limit") int limit);
}
