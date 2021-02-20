package com.destrostudios.turnbasedgametools.network.client.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.modules.game.GameService;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGameData<S, A> {

    private static final Logger LOG = LoggerFactory.getLogger(ClientGameData.class);

    private final UUID id;
    public int version;
    private final Queue<ActionReplay<A>> pendingActions = new ConcurrentLinkedQueue<>();
    private S state;
    private boolean desynced = false;

    public ClientGameData(UUID id, int version, S state) {
        this.id = id;
        this.version = version;
        this.state = state;
    }

    public void enqueueAction(A action, int version, int[] randomHistory) {
        pendingActions.offer(new ActionReplay<>(action, version, randomHistory));
    }

    public boolean applyNextAction(GameService<S, A> service) {
        try {
            ActionReplay<A> actionReplay = pendingActions.poll();
            if (actionReplay != null) {
                if (actionReplay.version != version) {
                    LOG.error("Action is: {}, remaining action queue: {}", actionReplay, pendingActions);
                    throw new IllegalStateException("Action version mismatch, expected: " + version + ", actual: " + actionReplay.version + ".");
                }
                state = service.applyAction(state, actionReplay.action, new SlaveRandom(actionReplay.randomHistory));
                version++;
                return true;
            }
        } catch (Throwable t) {
            desynced = true;
            throw t;
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

}
