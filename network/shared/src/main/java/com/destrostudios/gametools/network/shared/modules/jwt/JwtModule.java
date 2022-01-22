package com.destrostudios.gametools.network.shared.modules.jwt;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Login;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Logout;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogin;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogout;
import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.esotericsoftware.kryo.Kryo;

public abstract class JwtModule extends NetworkModule {
    @Override
    public void initialize(Kryo kryo) {
        kryo.register(Login.class);
        kryo.register(Logout.class);
        kryo.register(UserLogin.class);
        kryo.register(UserLogout.class);

        kryo.register(JwtAuthenticationUser.class);
    }
}
