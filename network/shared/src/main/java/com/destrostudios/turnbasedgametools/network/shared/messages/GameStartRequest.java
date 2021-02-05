package com.destrostudios.turnbasedgametools.network.shared.messages;

public class GameStartRequest {

    public Class<?> gameType;

    GameStartRequest() {
    }

    public GameStartRequest(Class<?> gameType) {
        this.gameType = gameType;
    }
}
