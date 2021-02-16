package com.destrostudios.turnbasedgametools.network.samples.game;

import com.destrostudios.turnbasedgametools.network.BlockingMessageModule;
import com.destrostudios.turnbasedgametools.network.client.ToolsClient;
import com.destrostudios.turnbasedgametools.network.client.modules.game.ClientGameData;
import com.destrostudios.turnbasedgametools.network.client.modules.game.GameClientModule;
import com.destrostudios.turnbasedgametools.network.client.modules.game.GameStartClientModule;
import com.destrostudios.turnbasedgametools.network.samples.game.connect4.Connect4Impl;
import com.destrostudios.turnbasedgametools.network.samples.game.connect4.Connect4Service;
import com.destrostudios.turnbasedgametools.network.samples.game.connect4.Connect4StartInfo;
import com.destrostudios.turnbasedgametools.network.server.ToolsServer;
import com.destrostudios.turnbasedgametools.network.server.modules.game.GameServerModule;
import com.destrostudios.turnbasedgametools.network.server.modules.game.GameStartServerModule;
import com.destrostudios.turnbasedgametools.network.server.modules.game.ServerGameData;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.GameService;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameJoin;
import com.destrostudios.turnbasedgametools.network.shared.modules.ping.PingModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.ping.messages.Ping;
import com.destrostudios.turnbasedgametools.network.shared.modules.ping.messages.Pong;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkGameIT {

    private ToolsServer server;
    private ToolsClient client;

    @Before
    public void setup() throws IOException {
        Server kryoServer = new Server();
        GameService<Connect4Impl, Long> gameService = new Connect4Service();
        Consumer<Kryo> registerParams = kryo -> kryo.register(Connect4StartInfo.class);
        GameServerModule<Connect4Impl, Long> gameServerModule = new GameServerModule<>(gameService, kryoServer::getConnections);
        GameStartServerModule<Connect4StartInfo> gameStartServerModule = new GameStartServerModule<>(registerParams) {
            @Override
            public void startGameRequest(Connection connection, Connect4StartInfo params) {
                UUID gameId = UUID.randomUUID();
                gameServerModule.registerGame(new ServerGameData<>(gameId, new Connect4Impl(params.width, params.height), new Random(3)));
                for (Connection other : kryoServer.getConnections()) {
                    gameServerModule.join(other, gameId);
                }
            }
        };

        server = new ToolsServer(kryoServer, gameServerModule, gameStartServerModule, new PingModule());
        server.start(NetworkUtil.PORT);

        Client kryoClient = new Client();
        GameClientModule<Connect4Impl, Long> gameClientModule = new GameClientModule<>(gameService, kryoClient);
        GameStartClientModule<Connect4StartInfo> gameStartClientModule = new GameStartClientModule<>(registerParams, kryoClient);
        client = new ToolsClient(kryoClient, gameClientModule, gameStartClientModule, new PingModule(), new BlockingMessageModule());
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
        GameStartClientModule<Connect4StartInfo> gameStartClientModule = client.getModule(GameStartClientModule.class);
        BlockingMessageModule block = client.getModule(BlockingMessageModule.class);

        int pointer = 0;
        long[] actions = {1L, 2L, 128L};

        gameStartClientModule.startNewGame(new Connect4StartInfo());
        block.takeUntil(GameJoin.class);
        ClientGameData<Connect4Impl, Long> game = gameClient.getJoinedGames().get(0);
        while (pointer < actions.length) {
            long action = actions[pointer++];
            gameClient.sendAction(game.getId(), action);
            block.takeUntil(GameAction.class);
            gameClient.applyAllActions(game.getId());
            System.out.println();
            System.out.println(game.getState());
        }

        assertEquals(129L, game.getState().white());
        assertEquals(2L, game.getState().black());
    }

    @Test(timeout = 1000)
    public void rollbackAction() throws InterruptedException {
        GameClientModule<Connect4Impl, Long> gameClient = client.getModule(GameClientModule.class);
        GameStartClientModule<Connect4StartInfo> gameStartClientModule = client.getModule(GameStartClientModule.class);
        BlockingMessageModule block = client.getModule(BlockingMessageModule.class);

        gameStartClientModule.startNewGame(new Connect4StartInfo());
        block.takeUntil(GameJoin.class);
        ClientGameData<Connect4Impl, Long> game = gameClient.getJoinedGames().get(0);
        gameClient.sendAction(game.getId(), 1L);
        block.takeUntil(GameAction.class);
        gameClient.applyAllActions(game.getId());
        gameClient.sendAction(game.getId(), ~0L);
        client.getKryoClient().sendTCP(new Ping());

        Object obj = block.takeNext();
        assertTrue(obj instanceof Pong);// this guarantees that there was no response to the invalid action

        System.out.println();
        System.out.println(game.getState());

        assertEquals(1L, game.getState().white());
        assertEquals(0L, game.getState().black());
    }

    @Test(timeout = 1000)
    public void recoverFromDesync() throws InterruptedException {
        GameClientModule<Connect4Impl, Long> gameClient = client.getModule(GameClientModule.class);
        GameStartClientModule<Connect4StartInfo> gameStartClientModule = client.getModule(GameStartClientModule.class);
        BlockingMessageModule block = client.getModule(BlockingMessageModule.class);

        gameStartClientModule.startNewGame(new Connect4StartInfo());
        block.takeUntil(GameJoin.class);
        ClientGameData<Connect4Impl, Long> game = gameClient.getJoinedGames().get(0);
        game.getState().own = ~0;
        gameClient.sendAction(game.getId(), 1L);
        block.takeUntil(GameAction.class);
        boolean updated = gameClient.applyAllActions(game.getId());
        assertFalse(updated);
        assertTrue(game.isDesynced());
        block.takeUntil(GameJoin.class);
        ClientGameData<Connect4Impl, Long> resyncedGame = gameClient.getJoinedGame(game.getId());
        assertFalse(resyncedGame.isDesynced());
        System.out.println();
        System.out.println(resyncedGame.getState());

        assertEquals(1L, resyncedGame.getState().white());
        assertEquals(0L, resyncedGame.getState().black());
    }
}
