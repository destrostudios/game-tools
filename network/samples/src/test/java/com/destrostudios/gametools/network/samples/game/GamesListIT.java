package com.destrostudios.gametools.network.samples.game;

import com.destrostudios.gametools.network.samples.game.connect4.Connect4StartInfo;
import com.destrostudios.gametools.network.server.ToolsServer;
import com.destrostudios.gametools.network.shared.modules.ping.PingModule;
import com.destrostudios.gametools.network.BlockingMessageModule;
import com.destrostudios.gametools.network.client.ToolsClient;
import com.destrostudios.gametools.network.client.modules.game.GameStartClientModule;
import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.gametools.network.server.modules.game.GameStartServerModule;
import com.destrostudios.gametools.network.server.modules.game.LobbyServerModule;
import com.destrostudios.gametools.network.shared.NetworkUtil;
import com.destrostudios.gametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.gametools.network.shared.modules.game.messages.UnlistGame;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GamesListIT {

    private ToolsServer server;
    private ToolsClient client;

    @BeforeEach
    public void setup() throws IOException {
        Server kryoServer = new Server();
        Consumer<Kryo> registerParams = kryo -> kryo.register(Connect4StartInfo.class);
        LobbyServerModule<Connect4StartInfo> lobbyServerModule = new LobbyServerModule<>(registerParams, kryoServer::getConnections);
        GameStartServerModule<Connect4StartInfo> gameStartServerModule = new GameStartServerModule<>(registerParams) {
            @Override
            public void startGameRequest(Connection connection, Connect4StartInfo params) {
                lobbyServerModule.listGame(UUID.randomUUID(), params);
            }
        };


        server = new ToolsServer(kryoServer, lobbyServerModule, gameStartServerModule, new PingModule());
        server.start(NetworkUtil.PORT);

        Client kryoClient = new Client();
        client = new ToolsClient(kryoClient, new LobbyClientModule<>(registerParams, kryoClient), new GameStartClientModule<>(registerParams, kryoClient), new PingModule(), new BlockingMessageModule());
        client.start(1000, "localhost", NetworkUtil.PORT);
    }

    @AfterEach
    public void cleanup() {
        client.stop();
        server.stop();

        server = null;
        client = null;
    }

    @Test
    @Timeout(1)
    public void sampleGame() throws InterruptedException {
        LobbyClientModule<Connect4StartInfo> lobbyClient = client.getModule(LobbyClientModule.class);
        GameStartClientModule<Connect4StartInfo> startClient = client.getModule(GameStartClientModule.class);
        BlockingMessageModule block = client.getModule(BlockingMessageModule.class);

        startClient.startNewGame(new Connect4StartInfo());
        lobbyClient.subscribeToGamesList();
        block.takeUntil(ListGame.class);
        assertEquals(1, lobbyClient.getListedGames().size());

        startClient.startNewGame(new Connect4StartInfo());
        block.takeUntil(ListGame.class);
        assertEquals(2, lobbyClient.getListedGames().size());

        lobbyClient.unsubscribeFromGamesList();
        block.takeUntil(UnlistGame.class);
        block.takeUntil(UnlistGame.class);
        assertEquals(0, lobbyClient.getListedGames().size());
    }

}
