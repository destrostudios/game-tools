package com.destrostudios.turnbasedgametools.network.client;

import java.util.UUID;

public class ClientGameData<S, A> {
    public final UUID id;
    public S state;

    public ClientGameData(UUID id, S state) {
        this.id = id;
        this.state = state;
    }
}
