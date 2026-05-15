package com.example.springbootapp.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.User;

@Mapper
public interface UserMapper {

	List<User> findAll();

	Optional<User> findById(@Param("id") String id);

	boolean existsById(@Param("id") String id);

	boolean existsByEmail(@Param("email") String email);

	int insert(User user);

	int update(User user);

	int deleteById(@Param("id") String id);
}
