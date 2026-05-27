package com.example.springbootapp.service;

import com.example.springbootapp.auth.AppRole;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.MenuAccessSnapshot;
import com.example.springbootapp.domain.BizCompany;
import com.example.springbootapp.mapper.BizCompanyMapper;
import com.example.springbootapp.mapper.UserMapper;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * 헤더 사용자 메뉴 등에 표시할 계정·권한 요약 문자열을 만듭니다.
 */
@Service
public class LoginAuthDisplayService {

	private static final DateTimeFormatter LOGIN_AT_FMT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREA);

	private final BizCompanyMapper bizCompanyMapper;
	private final UserMapper userMapper;

	public LoginAuthDisplayService(BizCompanyMapper bizCompanyMapper, UserMapper userMapper) {
		this.bizCompanyMapper = bizCompanyMapper;
		this.userMapper = userMapper;
	}

	public void enrichUserDropdown(Model model, LoginSession login) {
		if (login == null) {
			model.addAttribute("userDisplayName", "");
			model.addAttribute("userInitial", "");
			model.addAttribute("userProfileImageUrl", "");
			model.addAttribute("authRoleTheme", "default");
			model.addAttribute("authRoleLabel", "");
			model.addAttribute("authWriteLabel", "");
			model.addAttribute("authCompanySummary", "");
			model.addAttribute("authMenuCount", 0);
			model.addAttribute("loginTypeLabel", "");
			model.addAttribute("loginAtText", "");
			return;
		}
		String displayName = resolveDisplayName(login);
		model.addAttribute("userDisplayName", displayName);
		model.addAttribute("userInitial", initialFrom(displayName));
		model.addAttribute("userProfileImageUrl", resolveProfileImageUrl(login.getUserId()));
		MenuAccessSnapshot access = login.getMenuAccess();
		AppRole role = access != null ? access.role() : null;

		model.addAttribute("authRoleLabel", roleLabel(role, access));
		model.addAttribute("authRoleTheme", roleTheme(role, access));
		model.addAttribute("authWriteLabel", writeLabel(access));
		model.addAttribute("authCompanySummary", companySummary(access));
		model.addAttribute(
				"authMenuCount",
				access != null && access.getAllowedMenuPaths() != null
						? access.getAllowedMenuPaths().size()
						: 0);
		model.addAttribute("loginTypeLabel", loginTypeLabel(login.getLoginType()));
		model.addAttribute(
				"loginAtText",
				login.getLoginAt() != null ? LOGIN_AT_FMT.format(login.getLoginAt()) : "");
		if (access != null) {
			model.addAttribute("authRoleCd", access.getRoleCd());
			model.addAttribute("authCanWriteAll", access.isWriteAll());
		}
	}

	private String roleTheme(AppRole role, MenuAccessSnapshot access) {
		if (role == AppRole.PLATFORM_ADMIN || (access != null && access.isWriteAll())) {
			return "platform";
		}
		if (role == AppRole.COMPANY_ADMIN) {
			return "company";
		}
		if (role == AppRole.COMPANY_CUSTOMER) {
			return "customer";
		}
		return "default";
	}

	private String roleLabel(AppRole role, MenuAccessSnapshot access) {
		if (role == AppRole.PLATFORM_ADMIN) {
			return "플랫폼 관리자";
		}
		if (role == AppRole.COMPANY_ADMIN) {
			return "업체 관리자";
		}
		if (role == AppRole.COMPANY_CUSTOMER) {
			return "업체 고객";
		}
		if (access != null && access.isWriteAll()) {
			return "플랫폼 관리자";
		}
		return "권한 미지정";
	}

	private String writeLabel(MenuAccessSnapshot access) {
		if (access == null) {
			return "—";
		}
		if (access.isWriteAll()) {
			return "전체 화면·API 수정";
		}
		AppRole role = access.role();
		if (role == AppRole.COMPANY_ADMIN) {
			return "업체·쇼핑몰 운영 수정";
		}
		if (role == AppRole.COMPANY_CUSTOMER) {
			return "조회만 (수정 불가)";
		}
		return "—";
	}

	private String companySummary(MenuAccessSnapshot access) {
		if (access == null) {
			return "—";
		}
		if (access.isWriteAll() || access.role() == AppRole.PLATFORM_ADMIN) {
			return "전체 업체";
		}
		List<Long> ids = access.getAllowedCompanyIds();
		if (ids == null || ids.isEmpty()) {
			return "연결 업체 없음 (기본 메뉴)";
		}
		List<String> names = new ArrayList<>();
		for (Long id : ids) {
			if (id == null) {
				continue;
			}
			BizCompany company = bizCompanyMapper.findById(id);
			if (company != null && company.getCompanyNm() != null && !company.getCompanyNm().isBlank()) {
				names.add(company.getCompanyNm() + " (#" + id + ")");
			} else {
				names.add("#" + id);
			}
		}
		return names.isEmpty() ? "연결 업체 " + ids.size() + "곳" : String.join(", ", names);
	}

	private String resolveProfileImageUrl(String userId) {
		if (userId == null || userId.isBlank()) {
			return "";
		}
		return userMapper.findById(userId.trim())
				.map(u -> u.getProfileImageUrl() != null ? u.getProfileImageUrl().trim() : "")
				.filter(url -> !url.isEmpty())
				.orElse("");
	}

	private String resolveDisplayName(LoginSession login) {
		if (login.getUserName() != null && !login.getUserName().isBlank()) {
			return login.getUserName().trim();
		}
		return login.getUserId() != null ? login.getUserId().trim() : "";
	}

	private String initialFrom(String displayName) {
		if (displayName == null || displayName.isEmpty()) {
			return "U";
		}
		return displayName.substring(0, 1);
	}

	private String loginTypeLabel(String loginType) {
		if (loginType == null || loginType.isBlank()) {
			return "—";
		}
		return switch (loginType.toUpperCase(Locale.ROOT)) {
			case "FORM" -> "일반 로그인";
			case "KAKAO" -> "카카오";
			case "NAVER" -> "네이버";
			default -> loginType;
		};
	}
}
