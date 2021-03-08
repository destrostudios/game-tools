package com.destrostudios.turnbasedgametools.network.samples.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.turnbasedgametools.network.BlockingMessageModule;
import com.destrostudios.turnbasedgametools.network.client.ToolsClient;
import com.destrostudios.turnbasedgametools.network.client.modules.jwt.JwtClientModule;
import com.destrostudios.turnbasedgametools.network.server.ToolsServer;
import com.destrostudios.turnbasedgametools.network.server.modules.jwt.JwtServerModule;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import com.destrostudios.turnbasedgametools.network.shared.modules.jwt.messages.UserLogin;
import com.destrostudios.turnbasedgametools.network.shared.modules.jwt.messages.UserLogout;
import com.destrostudios.turnbasedgametools.network.shared.modules.ping.PingModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.ping.messages.Ping;
import com.destrostudios.turnbasedgametools.network.shared.modules.ping.messages.Pong;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OnlinePlayersIT {

    private ToolsServer server;
    private ToolsClient[] clients;

    @Before
    public void setup() throws IOException {
        NoValidateJwtService jwtService = new NoValidateJwtService();

        Server kryoServer = new Server();
        server = new ToolsServer(kryoServer, new JwtServerModule(jwtService, kryoServer::getConnections), new PingModule());
        server.start(NetworkUtil.PORT);

        clients = new ToolsClient[3];
        for (int i = 0; i < clients.length; i++) {
            Client kryoClient = new Client();
            clients[i] = new ToolsClient(kryoClient, new JwtClientModule(jwtService, kryoClient), new BlockingMessageModule(), new PingModule());
            clients[i].start(1000, "localhost", NetworkUtil.PORT);
        }
    }

    @After
    public void cleanup() {
        for (ToolsClient client : clients) {
            client.stop();
        }
        server.stop();

        server = null;
        clients = null;
    }

    @Test(timeout = 1000)
    public void alone() throws InterruptedException {
        BlockingMessageModule blockModule0 = clients[0].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule0 = clients[0].getModule(JwtClientModule.class);
        JwtAuthenticationUser user0 = new JwtAuthenticationUser(178, "TestName");

        jwtModule0.login(createJwt(user0));
        UserLogin userLogin = blockModule0.takeUntil(UserLogin.class);
        assertEquals(user0, userLogin.user);
        assertEquals(Collections.singletonList(user0), jwtModule0.getOnlineUsers());
    }

    @Test(timeout = 1000)
    public void two() throws InterruptedException {
        BlockingMessageModule blockModule0 = clients[0].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule0 = clients[0].getModule(JwtClientModule.class);
        JwtAuthenticationUser user0 = new JwtAuthenticationUser(178, "TestName");

        BlockingMessageModule blockModule1 = clients[1].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule1 = clients[1].getModule(JwtClientModule.class);
        JwtAuthenticationUser user1 = new JwtAuthenticationUser(5425, "Frank");

        jwtModule0.login(createJwt(user0));
        jwtModule1.login(createJwt(user1));

        blockModule0.takeUntil(UserLogin.class);
        blockModule0.takeUntil(UserLogin.class);
        blockModule1.takeUntil(UserLogin.class);
        blockModule1.takeUntil(UserLogin.class);

        List<JwtAuthenticationUser> expected = List.of(user0, user1);

        assertEquals(expected, jwtModule0.getOnlineUsers());
        assertEquals(expected, jwtModule1.getOnlineUsers());

        jwtModule0.logout();
        blockModule1.takeUntil(UserLogout.class);

        expected = Collections.singletonList(user1);
        assertEquals(expected, jwtModule1.getOnlineUsers());
    }

    @Test(timeout = 1000)
    public void multiConnect() throws InterruptedException {
        BlockingMessageModule blockModule0 = clients[0].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule0 = clients[0].getModule(JwtClientModule.class);
        JwtAuthenticationUser user0 = new JwtAuthenticationUser(178, "TestName");

        BlockingMessageModule blockModule1 = clients[1].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule1 = clients[1].getModule(JwtClientModule.class);
        JwtAuthenticationUser user1 = new JwtAuthenticationUser(5425, "Frank");

        BlockingMessageModule blockModule2 = clients[2].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule2 = clients[2].getModule(JwtClientModule.class);
        JwtAuthenticationUser user2 = user0; // multiple connections with same account

        jwtModule0.login(createJwt(user0));
        jwtModule1.login(createJwt(user1));
        jwtModule2.login(createJwt(user2));

        blockModule0.takeUntil(UserLogin.class);
        blockModule0.takeUntil(UserLogin.class);
        blockModule1.takeUntil(UserLogin.class);
        blockModule1.takeUntil(UserLogin.class);
        blockModule2.takeUntil(UserLogin.class);
        blockModule2.takeUntil(UserLogin.class);

        List<JwtAuthenticationUser> expected = List.of(user0, user1);

        assertEquals(expected, jwtModule0.getOnlineUsers());
        assertEquals(expected, jwtModule1.getOnlineUsers());

        jwtModule2.logout();
        clients[2].getKryoClient().sendTCP(new Ping());
        blockModule2.takeUntil(Pong.class);
        clients[0].getKryoClient().sendTCP(new Ping());
        blockModule0.takeUntil(Pong.class);
        clients[1].getKryoClient().sendTCP(new Ping());
        blockModule1.takeUntil(Pong.class);

        assertEquals(expected, jwtModule0.getOnlineUsers());
        assertEquals(expected, jwtModule1.getOnlineUsers());
    }

    @Test(timeout = 1000)
    public void disconnect() throws InterruptedException {
        BlockingMessageModule blockModule0 = clients[0].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule0 = clients[0].getModule(JwtClientModule.class);
        JwtAuthenticationUser user0 = new JwtAuthenticationUser(178, "TestName");

        BlockingMessageModule blockModule1 = clients[1].getModule(BlockingMessageModule.class);
        JwtClientModule jwtModule1 = clients[1].getModule(JwtClientModule.class);
        JwtAuthenticationUser user1 = new JwtAuthenticationUser(5425, "Frank");

        jwtModule0.login(createJwt(user0));
        jwtModule1.login(createJwt(user1));

        blockModule0.takeUntil(UserLogin.class);
        blockModule0.takeUntil(UserLogin.class);
        blockModule1.takeUntil(UserLogin.class);
        blockModule1.takeUntil(UserLogin.class);

        List<JwtAuthenticationUser> expected = List.of(user0, user1);

        assertEquals(expected, jwtModule0.getOnlineUsers());
        assertEquals(expected, jwtModule1.getOnlineUsers());

        clients[0].stop();
        blockModule1.takeUntil(UserLogout.class);

        expected = Collections.singletonList(user1);
        assertEquals(expected, jwtModule1.getOnlineUsers());
    }

    private static String createJwt(JwtAuthenticationUser user) {
        Map<String, ?> map = Map.of("id", user.id, "login", user.login);
        return JWT.create()
                .withIssuedAt(new Date())
                .withClaim("user", map)
                .sign(Algorithm.none());
    }
}
