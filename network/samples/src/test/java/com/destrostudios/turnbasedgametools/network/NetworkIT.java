package com.destrostudios.turnbasedgametools.network;

import com.destrostudios.turnbasedgametools.network.client.ClientGameData;
import com.destrostudios.turnbasedgametools.network.client.GamesClient;
import com.destrostudios.turnbasedgametools.network.samples.connect4.Connect4Impl;
import com.destrostudios.turnbasedgametools.network.samples.connect4.Connect4Service;
import com.destrostudios.turnbasedgametools.network.server.GamesServer;
import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameJoinAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.Ping;
import com.destrostudios.turnbasedgametools.network.shared.messages.Pong;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkIT {

    private GameService<Connect4Impl, Long> gameService;
    private GamesServer<Connect4Impl, Long> server;
    private GamesClient<Connect4Impl, Long> client;

    @Before
    public void setup() throws IOException {
        gameService = new Connect4Service();
        server = new GamesServer<>(NetworkUtil.PORT, gameService);
        client = new GamesClient<>("localhost", NetworkUtil.PORT, 100, gameService);
    }

    @After
    public void cleanup() {
        client.stop();
        server.stop();
    }

    @Test(timeout = 1000)
    public void sampleGame() throws InterruptedException {
        // used to block thread until tcp message was handled
        ArrayBlockingQueue<Object> block = new ArrayBlockingQueue<>(100);
        client.addConnectionListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                block.add(object);
            }
        });

        int pointer = 0;
        long[] actions = {1L, 2L, 128L};

        client.startNewGame();
        while (!(block.take() instanceof GameJoinAck)) {
            // block until we received the game state
        }
        ClientGameData<Connect4Impl, Long> game = client.getGames().get(0);
        while (pointer < actions.length) {
            long action = actions[pointer++];
            client.sendAction(game.getId(), action);
            while (!(block.take() instanceof GameAction)) {
                // block until we received the action
            }
            client.applyAllActions(game.getId());
            System.out.println();
            System.out.println(game.getState());
        }

        assertEquals(129L, game.getState().white());
        assertEquals(2L, game.getState().black());
    }

    @Test(timeout = 1000)
    public void rollbackAction() throws InterruptedException {
        // used to block thread until tcp message was handled
        ArrayBlockingQueue<Object> block = new ArrayBlockingQueue<>(100);
        client.addConnectionListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                block.add(object);
            }
        });

        client.startNewGame();
        while (!(block.take() instanceof GameJoinAck)) {
            // block until we received the game state
        }
        ClientGameData<Connect4Impl, Long> game = client.getGames().get(0);
        client.sendAction(game.getId(), 1L);
        while (!(block.take() instanceof GameAction)) {
            // block until we received the action
        }
        client.applyAllActions(game.getId());
        client.sendAction(game.getId(), ~0L);
        client.getKryoClient().sendTCP(new Ping());

        Object obj = block.take();
        assertTrue(obj instanceof Pong);// this guarantees that there was no response to the invalid action

        System.out.println();
        System.out.println(game.getState());

        assertEquals(1L, game.getState().white());
        assertEquals(0L, game.getState().black());
    }

    @Test(timeout = 1000)
    public void recoverFromDesync() throws InterruptedException {
        // used to block thread until tcp message was handled
        ArrayBlockingQueue<Object> block = new ArrayBlockingQueue<>(100);
        client.addConnectionListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                block.add(object);
            }
        });

        client.startNewGame();
        while (!(block.take() instanceof GameJoinAck)) {
            // block until we received the game state
        }
        ClientGameData<Connect4Impl, Long> game = client.getGames().get(0);
        game.getState().own = ~0;
        client.sendAction(game.getId(), 1L);
        while (!(block.take() instanceof GameAction)) {
            // block until we received the action
        }
        boolean updated = client.applyAllActions(game.getId());
        assertFalse(updated);
        assertTrue(game.isDesynced());
        while (!(block.take() instanceof GameJoinAck)) {
            // block until we received the game state
        }
        ClientGameData<Connect4Impl, Long> resyncedGame = client.getGame(game.getId());
        assertFalse(resyncedGame.isDesynced());
        System.out.println();
        System.out.println(resyncedGame.getState());

        assertEquals(1L, resyncedGame.getState().white());
        assertEquals(0L, resyncedGame.getState().black());
    }
}
