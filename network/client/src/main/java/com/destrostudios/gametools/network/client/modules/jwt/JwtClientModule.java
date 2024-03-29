package com.destrostudios.gametools.network.client.modules.jwt;

import com.destrostudios.authtoken.JwtAuthentication;
import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.authtoken.JwtService;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.shared.modules.jwt.JwtModule;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Login;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Logout;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogin;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogout;
import com.esotericsoftware.kryonet.Connection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class JwtClientModule extends JwtModule {

    private JwtAuthentication ownAuthentication;
    private final JwtService jwtService;
    private final Connection connection;
    private final Set<JwtAuthenticationUser> users = new TreeSet<>(Comparator.comparingLong(x -> x.id));

    public JwtClientModule(Connection connection) {
        this(new NoValidateJwtService(), connection);// no need to validate on client
    }

    public JwtClientModule(JwtService jwtService, Connection connection) {
        this.jwtService = jwtService;
        this.connection = connection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof UserLogin message) {
            users.add(message.user());
        } else if (object instanceof UserLogout message) {
            users.remove(message.user());
        }
    }

    @Override
    public void disconnected(Connection connection) {
        users.clear();
        ownAuthentication = null;
    }

    public void login(String jwt) {
        connection.sendTCP(new Login(jwt));
        ownAuthentication = jwtService.decode(jwt);
    }

    public void logout() {
        connection.sendTCP(new Logout());
        ownAuthentication = null;
    }

    public List<JwtAuthenticationUser> getOnlineUsers() {
        return List.copyOf(users);
    }

    public JwtAuthentication getOwnAuthentication() {
        return ownAuthentication;
    }
}
