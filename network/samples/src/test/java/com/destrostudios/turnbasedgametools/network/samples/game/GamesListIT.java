package com.destrostudios.turnbasedgametools.network.samples.game;

import com.destrostudios.turnbasedgametools.network.BlockingMessageModule;
import com.destrostudios.turnbasedgametools.network.client.ToolsClient;
import com.destrostudios.turnbasedgametools.network.client.modules.game.GameClientModule;
import com.destrostudios.turnbasedgametools.network.samples.game.connect4.Connect4Impl;
import com.destrostudios.turnbasedgametools.network.samples.game.connect4.Connect4Service;
import com.destrostudios.turnbasedgametools.network.server.ToolsServer;
import com.destrostudios.turnbasedgametools.network.server.modules.game.GameServerModule;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.GameService;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnlistGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.ping.PingModule;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GamesListIT {

    private ToolsServer server;
    private ToolsClient client;

    @Before
    public void setup() throws IOException {
        GameService<Connect4Impl, Long> gameService = new Connect4Service();

        Server kryoServer = new Server();
        server = new ToolsServer(kryoServer, new GameServerModule<>(gameService, kryoServer::getConnections), new PingModule());
        server.start(NetworkUtil.PORT);

        Client kryoClient = new Client();
        client = new ToolsClient(kryoClient, new GameClientModule<>(gameService, kryoClient), new PingModule(), new BlockingMessageModule());
        client.start(1000, "localhost", NetworkUtil.PORT);
    }

    @After
    public void cleanup() {
        client.stop();
        server.stop();

        server = null;
        client = null;
    }

    @Test(timeout = 1000)
    public void sampleGame() throws InterruptedException {
        GameClientModule<Connect4Impl, Long> gameClient = client.getModule(GameClientModule.class);
        BlockingMessageModule block = client.getModule(BlockingMessageModule.class);

        gameClient.startNewGame();
        gameClient.subscribeToGamesList();
        block.takeUntil(ListGame.class);
        assertEquals(1, gameClient.getGamesList().size());

        gameClient.startNewGame();
        block.takeUntil(ListGame.class);
        assertEquals(2, gameClient.getGamesList().size());

        gameClient.unsubscribeFromGamesList();
        block.takeUntil(UnlistGame.class);
        block.takeUntil(UnlistGame.class);
        assertEquals(0, gameClient.getGamesList().size());
    }

}
