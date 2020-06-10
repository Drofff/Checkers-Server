package com.drofff.checkers.server.type;

import javax.validation.constraints.NotBlank;

public class UserActivation {

    @NotBlank(message = "User id is required")
    private String userId;

    @NotBlank(message = "Missing activation token")
    private String token;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}