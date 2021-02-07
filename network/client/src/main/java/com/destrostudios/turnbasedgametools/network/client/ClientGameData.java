package com.destrostudios.turnbasedgametools.network.client;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientGameData<S, A> {

    private boolean desynced = false;
    private final UUID id;
    private final Queue<ActionReplay<A>> pendingActions = new ConcurrentLinkedQueue<>();
    private final Set<Object> tags;
    private S state;

    public ClientGameData(UUID id, Set<Object> tags, S state) {
        this.id = id;
        this.tags = new LinkedHashSet<>(tags);
        this.state = state;
    }

    public void enqueueAction(A action, int[] randomHistory) {
        pendingActions.offer(new ActionReplay<>(action, randomHistory));
    }

    public boolean update(GameService<S, A> service) {
        boolean updated = false;
        ActionReplay<A> actionReplay;
        while ((actionReplay = pendingActions.poll()) != null) {
            state = service.applyAction(state, actionReplay.action, new SlaveRandom(actionReplay.randomHistory));
            updated = true;
        }
        return updated;
    }

    public S getState() {
        return state;
    }

    public UUID getId() {
        return id;
    }

    public boolean isDesynced() {
        return desynced;
    }

    public void setDesynced() {
        this.desynced = true;
    }

    public Set<Object> getTags() {
        return Collections.unmodifiableSet(tags);
    }
}
