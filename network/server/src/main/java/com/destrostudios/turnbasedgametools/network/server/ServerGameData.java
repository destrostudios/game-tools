package com.destrostudios.turnbasedgametools.network.server;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ServerGameData {
    public final UUID id;
    private final Set<Integer> spectatorConnectionIds = new HashSet<>();
    public Object state;
    public final Random random;

    public ServerGameData(UUID id, Object state, Random random) {
        this.id = id;
        this.state = state;
        this.random = random;
    }

    public boolean hasSpectator(int connectionId) {
        return spectatorConnectionIds.contains(connectionId);
    }

    public void addSpectator(int connectionId) {
        spectatorConnectionIds.add(connectionId);
    }

    public void removeSpectator(int connectionId) {
        spectatorConnectionIds.remove(connectionId);
    }
}
