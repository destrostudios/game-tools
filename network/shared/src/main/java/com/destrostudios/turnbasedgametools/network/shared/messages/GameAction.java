package com.destrostudios.turnbasedgametools.network.shared.messages;

import java.util.UUID;

public class GameAction {

    public UUID gameId;
    public Object action;
    public int[] randomHistory;

    GameAction() {
    }

    public GameAction(UUID gameId, Object action, int[] randomHistory) {
        this.gameId = gameId;
        this.action = action;
        this.randomHistory = randomHistory;
    }
}
