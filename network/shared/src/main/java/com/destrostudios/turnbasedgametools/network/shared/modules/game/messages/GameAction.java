package com.destrostudios.turnbasedgametools.network.shared.modules.game.messages;

import java.util.UUID;

public class GameAction {

    public UUID gameId;
    public int version;
    public Object action;
    public int[] randomHistory;

    GameAction() {
    }

    public GameAction(UUID gameId, int version, Object action, int[] randomHistory) {
        this.gameId = gameId;
        this.version = version;
        this.action = action;
        this.randomHistory = randomHistory;
    }
}
