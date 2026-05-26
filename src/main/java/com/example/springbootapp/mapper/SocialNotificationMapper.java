package com.example.springbootapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.springbootapp.domain.SocialNotification;

public interface SocialNotificationMapper {

	List<SocialNotification> findByUserNm(@Param("userNm") String userNm, @Param("limit") int limit);

	List<SocialNotification> search(
			@Param("userNm") String userNm,
			@Param("senderNm") String senderNm,
			@Param("readYn") String readYn,
			@Param("keyword") String keyword,
			@Param("limit") int limit);

	SocialNotification findById(@Param("notificationId") Long notificationId);

	int insert(SocialNotification row);

	int updateReadYn(@Param("notificationId") Long notificationId, @Param("readYn") String readYn);

	int markAllRead(@Param("userNm") String userNm);

	int deleteById(@Param("notificationId") Long notificationId);
}
