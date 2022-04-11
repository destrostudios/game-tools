package com.destrostudios.gametools.network.shared.modules.jwt.messages;

import com.destrostudios.authtoken.JwtAuthenticationUser;

public record UserLogout(JwtAuthenticationUser user) {
}
