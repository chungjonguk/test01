package com.example.springbootapp.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.domain.User;
import com.example.springbootapp.mapper.UserMapper;

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
	public void create(User user) {
		user.setRrno(rrnoCryptoService.encrypt(user.getRrno()));
		int rows = userMapper.insert(user);
		if (rows != 1) {
			throw new IllegalStateException("user 테이블 저장 실패: id=" + user.getId());
		}
		log.info("user 테이블 저장 완료: id={}, email={}", user.getId(), user.getEmail());
	}

	private void decryptRrno(User user) {
		user.setRrno(rrnoCryptoService.decrypt(user.getRrno()));
	}
}
