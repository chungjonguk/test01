package com.example.springbootapp.service;

import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.storage.NasMediaType;
import com.example.springbootapp.storage.NasStorageService;
import com.example.springbootapp.storage.NasStorageService.NasStoredFile;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserProfileImageService {

	/** 프로필 사진 최대 용량 (2MB) */
	public static final long PROFILE_MAX_BYTES = 2L * 1024 * 1024;
	/** 커버 이미지 최대 용량 (5MB) */
	public static final long COVER_MAX_BYTES = 5L * 1024 * 1024;

	private final UserMapper userMapper;
	private final NasStorageService nasStorageService;
	private final SessionAuthService sessionAuthService;

	public UserProfileImageService(
			UserMapper userMapper,
			NasStorageService nasStorageService,
			SessionAuthService sessionAuthService) {
		this.userMapper = userMapper;
		this.nasStorageService = nasStorageService;
		this.sessionAuthService = sessionAuthService;
	}

	public Map<String, Object> getForSession(HttpSession session) {
		String userId = requireLoginUserId(session);
		User user = userMapper.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("userId", user.getId());
		body.put("profileImageUrl", user.getProfileImageUrl());
		body.put("coverImageUrl", user.getCoverImageUrl());
		body.put("profileMaxBytes", PROFILE_MAX_BYTES);
		body.put("coverMaxBytes", COVER_MAX_BYTES);
		return body;
	}

	@Transactional
	public Map<String, Object> uploadProfile(HttpSession session, MultipartFile file) throws IOException {
		return upload(session, file, true);
	}

	@Transactional
	public Map<String, Object> uploadCover(HttpSession session, MultipartFile file) throws IOException {
		return upload(session, file, false);
	}

	private Map<String, Object> upload(HttpSession session, MultipartFile file, boolean profile)
			throws IOException {
		String userId = requireLoginUserId(session);
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("이미지 파일을 선택하세요.");
		}
		validateFileSize(file, profile);
		NasStoredFile stored = nasStorageService.store(NasMediaType.IMAGE, file, userId);
		if (profile) {
			userMapper.updateProfileImageUrl(userId, stored.url(), userId);
		} else {
			userMapper.updateCoverImageUrl(userId, stored.url(), userId);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("url", stored.url());
		body.put("profile", profile);
		return body;
	}

	private String requireLoginUserId(HttpSession session) {
		String userId = sessionAuthService.getLoginUserId(session);
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		return userId.trim();
	}

	public Optional<User> findUser(String userId) {
		return userMapper.findById(userId);
	}

	private void validateFileSize(MultipartFile file, boolean profile) {
		long max = profile ? PROFILE_MAX_BYTES : COVER_MAX_BYTES;
		if (file.getSize() > max) {
			int mb = (int) (max / 1024 / 1024);
			throw new IllegalArgumentException(
					(profile ? "프로필 사진" : "커버 이미지") + "은 " + mb + "MB 이하만 등록할 수 있습니다.");
		}
	}
}
