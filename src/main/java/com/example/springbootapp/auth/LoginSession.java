package com.example.springbootapp.auth;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HTTP 세션에 저장되는 로그인 사용자 정보.
 * 세션 키: {@link SessionAuthService#ATTR_LOGIN_USER}
 */
public class LoginSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private String email;
    /** FORM, KAKAO */
    private String loginType;
    private LocalDateTime loginAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public LocalDateTime getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(LocalDateTime loginAt) {
        this.loginAt = loginAt;
    }
}
