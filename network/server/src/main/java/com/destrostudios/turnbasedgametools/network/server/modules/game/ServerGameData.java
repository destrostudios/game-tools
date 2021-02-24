package com.destrostudios.turnbasedgametools.network.server.modules.game;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class ServerGameData<S> {
    public final UUID id;
    private final Set<Integer> connections = new CopyOnWriteArraySet<>();
    public S state;
    public final Random random;

    public ServerGameData(UUID id, S state, Random random) {
        this.id = id;
        this.state = state;
        this.random = random;
    }

    public boolean hasConnection(int connectionId) {
        return connections.contains(connectionId);
    }

    public void addConnection(int connectionId) {
        connections.add(connectionId);
    }

    public void removeConnection(int connectionId) {
        connections.remove(connectionId);
    }

    public Set<Integer> getConnections() {
        return Set.copyOf(connections);
    }
}
