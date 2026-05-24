package com.example.springbootapp.service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.dto.UserRegisterDto;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.util.PasswordPolicyValidator;
/**
 * 사용자 조회·중복 확인·회원가입 및 주민번호 복호화를 처리하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	private final UserMapper userMapper;
	private final RrnoCryptoService rrnoCryptoService;
	public UserService(UserMapper userMapper, RrnoCryptoService rrnoCryptoService) {
		this.userMapper = userMapper;
		this.rrnoCryptoService = rrnoCryptoService;
	}
	/**
	 * 전체 사용자 목록을 조회한다. 주민번호는 복호화하여 반환한다.
	 *
	 * @return 사용자 목록
	 */
	public List<User> findAll() {
		List<User> users = userMapper.findAll();
		users.forEach(this::decryptRrno);
		return users;
	}
	/**
	 * 아이디 중복 여부를 확인한다.
	 *
	 * @param id 사용자 아이디
	 * @return 이미 존재하면 true
	 */
	public boolean existsById(String id) {
		return userMapper.existsById(id);
	}
	/**
	 * 이메일 중복 여부를 확인한다.
	 *
	 * @param email 이메일 주소
	 * @return 이미 존재하면 true
	 */
	public boolean existsByEmail(String email) {
		return userMapper.existsByEmail(email);
	}
	/**
	 * 회원가입 DTO를 검증하고 신규 사용자를 등록한다.
	 *
	 * @param dto 회원가입 입력 정보
	 */
	@Transactional
	public void register(UserRegisterDto dto) {
		String id = trim(dto.getId());
		String pw = dto.getPw();
		String name = trim(dto.getName());
		String sex = trim(dto.getSex());
		String rrno = trim(dto.getRrno());
		String email = trim(dto.getEmail());
		String zipcode = trim(dto.getZipcode());
		String address = trim(dto.getAddress());
		String addressDetail = trim(dto.getAddressDetail());
		if (id.isEmpty() || pw == null || pw.isBlank() || name.isEmpty() || sex.isEmpty() || rrno.isEmpty()
				|| email.isEmpty() || zipcode.isEmpty() || address.isEmpty() || addressDetail.isEmpty()) {
			throw new IllegalArgumentException("모든 항목을 입력해 주세요.");
		}
		if (!PasswordPolicyValidator.isValid(pw)) {
			throw new IllegalArgumentException(PasswordPolicyValidator.requirementMessage());
		}
		if (!"남자".equals(sex) && !"여자".equals(sex)) {
			throw new IllegalArgumentException("성별은 남자 또는 여자 중 하나를 선택해 주세요.");
		}
		if (existsById(id)) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + id);
		}
		if (existsByEmail(email)) {
			throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
		}
		User user = new User();
		user.setId(id);
		user.setPw(pw);
		user.setName(name);
		user.setSex(sex);
		user.setRrno(rrno);
		user.setEmail(email);
		user.setZipcode(zipcode);
		user.setAddress(address);
		user.setAddressDetail(addressDetail);
		user.setUpdateId(id);
		create(user);
	}
	/**
	 * 사용자 엔티티를 DB에 저장한다. 주민번호는 저장 전 암호화한다.
	 *
	 * @param user 저장할 사용자 엔티티
	 */
	@Transactional
	public void create(User user) {
		user.setRrno(rrnoCryptoService.encrypt(user.getRrno()));
		int rows = userMapper.insert(user);
		if (rows != 1) {
			throw new IllegalStateException("user 테이블 저장 실패: id=" + user.getId());
		}
		log.info("user 테이블 저장 완료: id={}, email={}", user.getId(), user.getEmail());
	}
	private String trim(String value) {
		return value == null ? "" : value.trim();
	}
	private void decryptRrno(User user) {
		user.setRrno(rrnoCryptoService.decrypt(user.getRrno()));
	}
}
