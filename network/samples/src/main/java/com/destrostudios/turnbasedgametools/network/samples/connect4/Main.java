package com.destrostudios.turnbasedgametools.network.samples.connect4;

import com.destrostudios.turnbasedgametools.network.client.ClientGameData;
import com.destrostudios.turnbasedgametools.network.client.GamesClient;
import com.destrostudios.turnbasedgametools.network.server.GamesServer;
import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.KryoUtil;
import java.io.IOException;

public class Main {
    public static void main(String... args) throws IOException, InterruptedException {
        GameService<Connect4Impl, Long> gameService = new Connect4Service();
        GamesServer server = new GamesServer(KryoUtil.PORT, gameService);
        GamesClient client = new GamesClient("localhost", KryoUtil.PORT, 10_000, gameService);

        int pointer = 0;
        long[] actions = {1L << 0, 1L << 1, 1L << 7};

        client.startNewGame(Connect4Impl.class);
        Thread.sleep(1_000);
        while (pointer < actions.length) {
            long action = actions[pointer++];
            for (ClientGameData game : client.getGames()) {
                client.sendAction(game.id, action);
                Thread.sleep(1_000);
                System.out.println();
                System.out.println(game.state);
            }
        }
        client.stop();
        server.stop();
    }
}
