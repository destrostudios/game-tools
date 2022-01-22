package com.destrostudios.gametools.network.shared.modules.jwt.messages;

import com.destrostudios.authtoken.JwtAuthenticationUser;

public class UserLogout {
    public JwtAuthenticationUser user;

    UserLogout() {
    }

    public UserLogout(JwtAuthenticationUser user) {
        this.user = user;
    }
}
