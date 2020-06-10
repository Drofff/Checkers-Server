package com.drofff.checkers.server.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    PLAYER;

    @Override
    public String getAuthority() {
        return name();
    }

}
