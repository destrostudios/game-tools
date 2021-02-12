package com.destrostudios.turnbasedgametools.network.client.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.modules.game.GameService;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientGameData<S, A> {

    private boolean desynced = false;
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

    public boolean applyNextAction(GameService<S, A> service) {
        ActionReplay<A> actionReplay;
        if ((actionReplay = pendingActions.poll()) != null) {
            state = service.applyAction(state, actionReplay.action, new SlaveRandom(actionReplay.randomHistory));
            return true;
        }
        return false;
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

}
