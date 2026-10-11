package com.crm.vo;

import java.io.Serializable;
import java.util.List;

/**
 * Login Response VO
 *
 * @author CRM Team
 */
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    public LoginVO() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * Embedded user info in login response
     */
    public static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String username;
        private String realName;
        private List<String> roles;

        public UserInfo() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
