package com.destrostudios.turnbasedgametools.network.shared.modules.jwt.messages;

import com.destrostudios.authtoken.JwtAuthenticationUser;

public class UserLogin {
    public JwtAuthenticationUser user;

    UserLogin() {
    }

    public UserLogin(JwtAuthenticationUser user) {
        this.user = user;
    }
}
