package com.destrostudios.turnbasedgametools.network.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGameData<S, A> {
    public final UUID id;
    private final Map<Integer, Set<Object>> connectionTags = new ConcurrentHashMap<>();
    public S state;
    public final Random random;

    public ServerGameData(UUID id, S state, Random random) {
        this.id = id;
        this.state = state;
        this.random = random;
    }

    public boolean hasConnection(int connectionId) {
        return connectionTags.containsKey(connectionId);
    }

    public void setUntaggedConnection(int connectionId) {
        connectionTags.put(connectionId, Collections.emptySet());
    }

    public void setConnectionTags(int connectionId, Set<Object> tags) {
        connectionTags.put(connectionId, new HashSet<>(tags));
    }

    public void removeConnection(int connectionId) {
        connectionTags.remove(connectionId);
    }
}
