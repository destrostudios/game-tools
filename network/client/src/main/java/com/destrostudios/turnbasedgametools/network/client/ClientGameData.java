package com.destrostudios.turnbasedgametools.network.client;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientGameData<S, A> {
    private final UUID id;
    private final Queue<ActionReplay<A>> pendingActions = new ConcurrentLinkedQueue<>();
    private S state;

    public ClientGameData(UUID id, S state) {
        this.id = id;
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
}
