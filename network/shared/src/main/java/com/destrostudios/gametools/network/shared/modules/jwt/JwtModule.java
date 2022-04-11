package com.destrostudios.gametools.network.shared.modules.jwt;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Login;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Logout;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogin;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogout;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public abstract class JwtModule extends NetworkModule {
    @Override
    public void initialize(Kryo kryo) {
        kryo.register(Login.class, new RecordSerializer<>());
        kryo.register(Logout.class, new RecordSerializer<>());
        kryo.register(UserLogin.class, new RecordSerializer<>());
        kryo.register(UserLogout.class, new RecordSerializer<>());

        kryo.register(JwtAuthenticationUser.class, new Serializer<JwtAuthenticationUser>() {

            @Override
            public void write(Kryo kryo, Output output, JwtAuthenticationUser object) {
                output.writeLong(object.id);
                output.writeString(object.login);
            }

            @Override
            public JwtAuthenticationUser read(Kryo kryo, Input input, Class<JwtAuthenticationUser> type) {
                return new JwtAuthenticationUser(input.readLong(), input.readString());
            }
        });
    }
}
