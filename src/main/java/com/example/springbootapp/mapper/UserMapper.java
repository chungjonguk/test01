package com.example.springbootapp.mapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.User;
/**
 * 사용자(user) MyBatis Mapper.
 */
@Mapper
public interface UserMapper {
	/**
	 * 전체 사용자 목록을 조회합니다.
	 *
	 * @return 사용자 목록
	 */
	List<User> findAll();
	/**
	 * 로그인 ID로 사용자를 조회합니다.
	 *
	 * @param id 로그인 ID
	 * @return 사용자 Optional
	 */
	Optional<User> findById(@Param("id") String id);
	/**
	 * 이름과 이메일이 모두 일치하는 사용자의 로그인 ID를 조회합니다. (아이디 찾기)
	 *
	 * @param name  사용자 이름
	 * @param email 이메일
	 * @return 로그인 ID Optional
	 */
	Optional<String> findLoginIdByNameAndEmail(@Param("name") String name, @Param("email") String email);
	/**
	 * 로그인 ID 존재 여부를 확인합니다.
	 *
	 * @param id 로그인 ID
	 * @return 존재하면 {@code true}
	 */
	boolean existsById(@Param("id") String id);
	/**
	 * 이메일 중복 여부를 확인합니다.
	 *
	 * @param email 이메일
	 * @return 존재하면 {@code true}
	 */
	boolean existsByEmail(@Param("email") String email);

	boolean existsByEmailForOtherUser(@Param("email") String email, @Param("id") String id);

	int updateProfileSettings(User user);
	/**
	 * 사용자를 등록합니다.
	 *
	 * @param user 등록할 사용자
	 * @return 반영된 행 수
	 */
	int insert(User user);
	/**
	 * 사용자 정보를 수정합니다.
	 *
	 * @param user 수정할 사용자
	 * @return 반영된 행 수
	 */
	int update(User user);
	/**
	 * 비밀번호와 수정자만 갱신합니다.
	 *
	 * @param id       사용자 아이디
	 * @param pw       새 비밀번호
	 * @param updateId 수정자 아이디
	 * @return 반영된 행 수
	 */
	int updatePassword(@Param("id") String id, @Param("pw") String pw, @Param("updateId") String updateId);

	int updateProfileImageUrl(
			@Param("id") String id,
			@Param("profileImageUrl") String profileImageUrl,
			@Param("updateId") String updateId);

	int updateCoverImageUrl(
			@Param("id") String id,
			@Param("coverImageUrl") String coverImageUrl,
			@Param("updateId") String updateId);

	/**
	 * 로그인 ID로 사용자를 삭제합니다.
	 *
	 * @param id 로그인 ID
	 * @return 삭제된 행 수
	 */
	int deleteById(@Param("id") String id);
}
