package com.destrostudios.turnbasedgametools.network.samples.connect4;

import com.destrostudios.turnbasedgametools.network.client.ClientGameData;
import com.destrostudios.turnbasedgametools.network.client.GamesClient;
import com.destrostudios.turnbasedgametools.network.server.GamesServer;
import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import java.io.IOException;

public class Main {
    public static void main(String... args) throws IOException, InterruptedException {
        System.out.println("Unsafe access warnings are a known issue, see: https://github.com/EsotericSoftware/kryonet/issues/154");
        GameService<Connect4Impl, Long> gameService = new Connect4Service();
        GamesServer<Connect4Impl, Long> server = new GamesServer<>(NetworkUtil.PORT, gameService);
        GamesClient<Connect4Impl, Long> client = new GamesClient<>("localhost", NetworkUtil.PORT, 10_000, gameService);

        int pointer = 0;
        long[] actions = {1L, 1L << 1, 1L << 7};

        client.startNewGame();
        Thread.sleep(1_000);
        while (pointer < actions.length) {
            long action = actions[pointer++];
            for (ClientGameData<Connect4Impl, Long> game : client.getGames()) {
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
