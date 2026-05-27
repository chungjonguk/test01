package com.example.springbootapp.service;

import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
import com.example.springbootapp.domain.UserAuthProfile;
import com.example.springbootapp.mapper.UserAuthProfileMapper;
import com.example.springbootapp.mapper.UserCompanyMapper;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthProfileService {

	private final UserAuthProfileMapper userAuthProfileMapper;
	private final UserCompanyMapper userCompanyMapper;
	private final MenuAccessService menuAccessService;

	public UserAuthProfileService(
			UserAuthProfileMapper userAuthProfileMapper,
			UserCompanyMapper userCompanyMapper,
			MenuAccessService menuAccessService) {
		this.userAuthProfileMapper = userAuthProfileMapper;
		this.userCompanyMapper = userCompanyMapper;
		this.menuAccessService = menuAccessService;
	}

	public void attachAuthToSession(HttpSession session, String userId) {
		if (session == null || userId == null) {
			return;
		}
		Object raw = session.getAttribute(com.example.springbootapp.auth.SessionAuthService.ATTR_LOGIN_USER);
		if (raw instanceof LoginSession loginSession) {
			loginSession.setMenuAccess(menuAccessService.resolveForUser(userId));
			session.setAttribute(com.example.springbootapp.auth.SessionAuthService.ATTR_LOGIN_USER, loginSession);
		}
	}

	@Transactional
	public void saveProfile(
			String userId,
			String roleCd,
			List<Long> companyIds,
			String actor) {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("사용자 ID가 필요합니다.");
		}
		AppRole role = AppRole.fromCode(roleCd);
		if (role == null) {
			throw new IllegalArgumentException("올바른 권한 코드가 아닙니다.");
		}
		String resolvedActor = actor != null && !actor.isBlank() ? actor : "SYSTEM";
		UserAuthProfile profile = new UserAuthProfile();
		profile.setUserId(userId);
		profile.setRoleCd(role.name());
		profile.setUseYn("Y");
		profile.setRegId(resolvedActor);
		profile.setUpdateId(resolvedActor);
		userAuthProfileMapper.upsert(profile);

		userCompanyMapper.deleteByUserId(userId);
		if (companyIds != null) {
			for (Long companyId : companyIds) {
				if (companyId != null) {
					userCompanyMapper.insert(userId, companyId, resolvedActor, resolvedActor);
				}
			}
		}
	}

	public MenuAccessSnapshot findAccess(String userId) {
		return menuAccessService.resolveForUser(userId);
	}
}
