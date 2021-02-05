package com.destrostudios.turnbasedgametools.network.client;

import java.util.UUID;

public class ClientGameData {
    public final UUID id;
    public Object state;

    public ClientGameData(UUID id, Object state) {
        this.id = id;
        this.state = state;
    }
}
