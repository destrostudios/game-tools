package com.destrostudios.gametools.network.server.modules.jwt;

import com.destrostudios.authtoken.JwtAuthentication;
import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.authtoken.JwtService;
import com.destrostudios.gametools.network.shared.modules.jwt.JwtModule;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Login;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.Logout;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogin;
import com.destrostudios.gametools.network.shared.modules.jwt.messages.UserLogout;
import com.esotericsoftware.kryonet.Connection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtServerModule extends JwtModule {

    private static final Logger LOG = LoggerFactory.getLogger(JwtServerModule.class);

    private final Map<Integer, JwtAuthenticationUser> connectionToUser = new ConcurrentHashMap<>();
    private final JwtService jwtService;
    private final Supplier<Connection[]> connectionsSupply;

    public JwtServerModule(JwtService jwtService, Supplier<Connection[]> connectionsSupply) {
        this.jwtService = jwtService;
        this.connectionsSupply = connectionsSupply;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof Login message) {
            JwtAuthentication authentication = jwtService.decode(message.jwt());

            login(connection, authentication.user);
        } else if (object instanceof Logout) {
            for (JwtAuthenticationUser other : connectionToUser.values()) {
                connection.sendTCP(new UserLogout(other));
            }
            logout(connection);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        logout(connection);
    }

    public void login(Connection connection, JwtAuthenticationUser user) {
        Set<JwtAuthenticationUser> previousUsers = new TreeSet<>(Comparator.comparingLong(x -> x.id));
        previousUsers.addAll(connectionToUser.values());

        connectionToUser.put(connection.getID(), user);
        for (JwtAuthenticationUser other : previousUsers) {
            connection.sendTCP(new UserLogin(other));
        }
        if (!previousUsers.contains(user)) {
            for (Connection other : connectionsSupply.get()) {
                if (connectionToUser.containsKey(other.getID())) {
                    other.sendTCP(new UserLogin(user));
                }
            }
        }
        LOG.info("Connection {} logged in as: {} {}.", connection.getID(), user.id, user.login);
    }

    public void logout(Connection connection) {
        JwtAuthenticationUser user = connectionToUser.remove(connection.getID());
        LOG.info("Connection {} logged out.", connection.getID());

        if (user != null && !connectionToUser.containsValue(user)) {
            for (Connection other : connectionsSupply.get()) {
                if (connectionToUser.containsKey(other.getID())) {
                    other.sendTCP(new UserLogout(user));
                }
            }
        }
    }

    public JwtAuthenticationUser getUser(int connectionId) {
        return connectionToUser.get(connectionId);
    }
}
