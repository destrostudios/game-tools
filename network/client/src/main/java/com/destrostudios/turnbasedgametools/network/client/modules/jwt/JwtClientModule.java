package com.destrostudios.turnbasedgametools.network.client.modules.jwt;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.authtoken.JwtService;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.turnbasedgametools.network.shared.modules.jwt.JwtModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.jwt.messages.Login;
import com.destrostudios.turnbasedgametools.network.shared.modules.jwt.messages.Logout;
import com.destrostudios.turnbasedgametools.network.shared.modules.jwt.messages.UserLogin;
import com.destrostudios.turnbasedgametools.network.shared.modules.jwt.messages.UserLogout;
import com.esotericsoftware.kryonet.Connection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class JwtClientModule extends JwtModule {

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
        if (object instanceof UserLogin) {
            UserLogin message = (UserLogin) object;
            users.add(message.user);
        } else if (object instanceof UserLogout) {
            UserLogout message = (UserLogout) object;
            users.remove(message.user);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        users.clear();
    }

    public JwtAuthenticationUser decode(String jwt) {
        return jwtService.decode(jwt).user;
    }

    public void login(String jwt) {
        connection.sendTCP(new Login(jwt));
    }

    public void logout() {
        connection.sendTCP(new Logout());
    }

    public List<JwtAuthenticationUser> onlineUsers() {
        return List.copyOf(users);
    }
}
