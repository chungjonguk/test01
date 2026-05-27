package com.example.springbootapp.service;

import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.dto.UserProfileSettingsDto;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.util.PhoneNumberValidator;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserProfileSettingsService {

	public static final String PRIMARY_HOME = "HOME";
	public static final String PRIMARY_WORK = "WORK";

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private final UserMapper userMapper;
	private final SessionAuthService sessionAuthService;

	public UserProfileSettingsService(UserMapper userMapper, SessionAuthService sessionAuthService) {
		this.userMapper = userMapper;
		this.sessionAuthService = sessionAuthService;
	}

	public Map<String, Object> getForSession(HttpSession session) {
		String userId = requireUserId(session);
		User user = userMapper.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
		applyLegacyFallback(user);
		return toResponseMap(user);
	}

	@Transactional
	public Map<String, Object> update(HttpSession session, UserProfileSettingsDto dto) {
		String userId = requireUserId(session);
		userMapper.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

		String name = trim(dto.getName());
		String email = trim(dto.getEmail());
		if (userId.isEmpty()) {
			throw new IllegalArgumentException("아이디(필수 정보)를 확인할 수 없습니다. 다시 로그인해 주세요.");
		}
		if (name.isEmpty()) {
			throw new IllegalArgumentException("필수 정보를 입력해 주세요. (이름)");
		}
		if (email.isEmpty()) {
			throw new IllegalArgumentException("필수 정보를 입력해 주세요. (이메일)");
		}
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
		}
		if (userMapper.existsByEmailForOtherUser(email, userId)) {
			throw new IllegalArgumentException("이미 등록된 이메일입니다.");
		}

		String primaryType = normalizePrimaryType(dto.getPrimaryAddressType());

		String homeZip = trim(dto.getHomeZipcode());
		String homeAddr = trim(dto.getHomeAddress());
		String homeDetail = trim(dto.getHomeAddressDetail());
		String workZip = trim(dto.getWorkZipcode());
		String workAddr = trim(dto.getWorkAddress());
		String workDetail = trim(dto.getWorkAddressDetail());

		User user = new User();
		user.setId(userId);
		user.setName(name);
		user.setEmail(email);
		user.setHomeZipcode(emptyToNull(homeZip));
		user.setHomeAddress(emptyToNull(homeAddr));
		user.setHomeAddressDetail(emptyToNull(homeDetail));
		user.setHomePhone(PhoneNumberValidator.normalizeOptional(trim(dto.getHomePhone()), "자택 전화번호"));
		user.setWorkZipcode(emptyToNull(workZip));
		user.setWorkAddress(emptyToNull(workAddr));
		user.setWorkAddressDetail(emptyToNull(workDetail));
		user.setWorkPhone(PhoneNumberValidator.normalizeOptional(trim(dto.getWorkPhone()), "직장 전화번호"));
		user.setWorkCompanyName(emptyToNull(trim(dto.getWorkCompanyName())));
		user.setPrimaryAddressType(primaryType);
		user.setUpdateId(userId);
		syncPrimaryMirror(user);

		int rows = userMapper.updateProfileSettings(user);
		if (rows != 1) {
			throw new IllegalStateException("프로필 저장에 실패했습니다.");
		}

		sessionAuthService.refreshLoginUserName(session, name);

		User saved = userMapper.findById(userId)
				.orElseThrow(() -> new IllegalStateException("저장된 프로필을 조회할 수 없습니다."));
		Map<String, Object> body = toResponseMap(saved);
		body.put("message", "프로필이 저장되었습니다.");
		return body;
	}

	private Map<String, Object> toResponseMap(User user) {
		applyLegacyFallback(user);
		String primary = normalizePrimaryType(user.getPrimaryAddressType());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("userId", user.getId());
		body.put("name", nullToEmpty(user.getName()));
		body.put("email", nullToEmpty(user.getEmail()));
		body.put("homeZipcode", nullToEmpty(user.getHomeZipcode()));
		body.put("homeAddress", nullToEmpty(user.getHomeAddress()));
		body.put("homeAddressDetail", nullToEmpty(user.getHomeAddressDetail()));
		body.put("homePhone", nullToEmpty(user.getHomePhone()));
		body.put("workZipcode", nullToEmpty(user.getWorkZipcode()));
		body.put("workAddress", nullToEmpty(user.getWorkAddress()));
		body.put("workAddressDetail", nullToEmpty(user.getWorkAddressDetail()));
		body.put("workPhone", nullToEmpty(user.getWorkPhone()));
		body.put("workCompanyName", nullToEmpty(user.getWorkCompanyName()));
		body.put("primaryAddressType", primary);
		if (PRIMARY_WORK.equals(primary)) {
			body.put("zipcode", nullToEmpty(user.getWorkZipcode()));
			body.put("address", nullToEmpty(user.getWorkAddress()));
			body.put("addressDetail", nullToEmpty(user.getWorkAddressDetail()));
		} else {
			body.put("zipcode", nullToEmpty(user.getHomeZipcode()));
			body.put("address", nullToEmpty(user.getHomeAddress()));
			body.put("addressDetail", nullToEmpty(user.getHomeAddressDetail()));
		}
		return body;
	}

	private void applyLegacyFallback(User user) {
		if (isBlank(user.getHomeZipcode()) && !isBlank(user.getZipcode())) {
			user.setHomeZipcode(user.getZipcode());
			user.setHomeAddress(user.getAddress());
			user.setHomeAddressDetail(user.getAddressDetail());
		}
		if (isBlank(user.getPrimaryAddressType())) {
			user.setPrimaryAddressType(PRIMARY_HOME);
		}
	}

	private void syncPrimaryMirror(User user) {
		if (PRIMARY_WORK.equals(user.getPrimaryAddressType())) {
			user.setZipcode(user.getWorkZipcode());
			user.setAddress(user.getWorkAddress());
			user.setAddressDetail(user.getWorkAddressDetail());
		} else {
			user.setZipcode(user.getHomeZipcode());
			user.setAddress(user.getHomeAddress());
			user.setAddressDetail(user.getHomeAddressDetail());
		}
	}

	private String normalizePrimaryType(String value) {
		if (value != null && PRIMARY_WORK.equalsIgnoreCase(value.trim())) {
			return PRIMARY_WORK;
		}
		return PRIMARY_HOME;
	}

	private String requireUserId(HttpSession session) {
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (login == null || login.getUserId() == null || login.getUserId().isBlank()) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		return login.getUserId().trim();
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value.trim();
	}

	private String emptyToNull(String value) {
		return value == null || value.isEmpty() ? null : value;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
