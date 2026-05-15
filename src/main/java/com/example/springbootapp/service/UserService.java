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

	public List<User> findAll() {
		List<User> users = userMapper.findAll();
		users.forEach(this::decryptRrno);
		return users;
	}

	public boolean existsById(String id) {
		return userMapper.existsById(id);
	}

	public boolean existsByEmail(String email) {
		return userMapper.existsByEmail(email);
	}

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
