package com.example.springbootapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.domain.User;
import com.example.springbootapp.mapper.UserMapper;

@SpringBootTest
@Transactional
class UserServicePasswordResetTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserMapper userMapper;

	@Test
	void resetPasswordToInitial_setsPasswordSameAsUserId() {
		User user = new User();
		user.setId("reset_pw_user");
		user.setPw("OldPass1!");
		user.setName("테스트");
		user.setSex("남자");
		user.setRrno("900101-1234567");
		user.setEmail("reset_pw_user@test.com");
		user.setZipcode("06236");
		user.setAddress("서울특별시 강남구");
		user.setAddressDetail("101호");
		user.setUpdateId("admin");
		userMapper.insert(user);

		userService.resetPasswordToInitial("reset_pw_user", "admin");

		User updated = userMapper.findById("reset_pw_user").orElseThrow();
		assertThat(updated.getPw()).isEqualTo("reset_pw_user");
		assertThat(updated.getUpdateId()).isEqualTo("admin");
		
	}

	@Test
	void resetPasswordToInitial_rejectsUnknownUser() {
		assertThatThrownBy(() -> userService.resetPasswordToInitial("unknown_user", "admin"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("존재하지 않는 사용자");
	}
}
