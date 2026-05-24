package com.example.springbootapp.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.ScreenList;
/**
 * 화면 메뉴(screen_list) MyBatis Mapper.
 */
@Mapper
public interface ScreenListMapper {
	/**
	 * 사용 중인 화면 메뉴 전체를 조회합니다.
	 *
	 * @return 활성 화면 목록
	 */
	List<ScreenList> findAllActive();
	/**
	 * 관리자용 화면 메뉴를 조건 검색합니다.
	 *
	 * @param screenId 화면 ID (nullable)
	 * @param screenNm 화면명 (nullable)
	 * @param uriPath  URI 경로 (nullable)
	 * @param useYn    사용 여부 (nullable)
	 * @return 화면 목록
	 */
	List<ScreenList> findForAdmin(
			@Param("screenId") String screenId,
			@Param("screenNm") String screenNm,
			@Param("uriPath") String uriPath,
			@Param("useYn") String useYn);
	/**
	 * 화면 ID로 단건 조회합니다.
	 *
	 * @param screenId 화면 ID
	 * @return 화면 정보, 없으면 {@code null}
	 */
	ScreenList findByScreenId(@Param("screenId") String screenId);
	/**
	 * URI 경로로 화면 정보를 조회합니다.
	 *
	 * @param uriPath URI 경로
	 * @return 화면 정보, 없으면 {@code null}
	 */
	ScreenList findByUriPath(@Param("uriPath") String uriPath);
	/**
	 * 등록된 화면 메뉴 전체 건수를 조회합니다.
	 *
	 * @return 화면 건수
	 */
	int countAll();
	/**
	 * 화면 메뉴를 등록합니다.
	 *
	 * @param screen 등록할 화면
	 * @return 반영된 행 수
	 */
	int insert(ScreenList screen);
	/**
	 * 화면 메뉴를 수정합니다.
	 *
	 * @param screen 수정할 화면
	 * @return 반영된 행 수
	 */
	int update(ScreenList screen);
}
