package com.destrostudios.gametools.network.server.modules.game;

import com.esotericsoftware.kryonet.Connection;

public interface GameStartService<P> {

    void startGame(Connection connection, P params);
}
