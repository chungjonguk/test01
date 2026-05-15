package com.example.springbootapp.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.springbootapp.domain.User;

@SpringBootTest
@Transactional
class UserMapperTest {

	@Autowired
	private UserMapper userMapper;

	@Test
	void insert_savesToUserTable() {
		User user = new User();
		user.setId("mybatis_test");
		user.setPw("pass1234");
		user.setName("테스트");
		user.setSex("남");
		user.setRrno("900101-1234567");
		user.setEmail("mybatis_test@test.com");
		user.setZipcode("06236");
		user.setAddress("서울특별시 강남구 테스트로 1");
		user.setAddressDetail("101호");
		user.setUpdateId("mybatis_test");

		int rows = userMapper.insert(user);
		assertThat(rows).isEqualTo(1);

		List<User> users = userMapper.findAll();
		assertThat(users).anyMatch(u -> "mybatis_test".equals(u.getId()));
	}
}
