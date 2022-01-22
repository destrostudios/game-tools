package com.destrostudios.gametools.network.client.modules.game;

import com.destrostudios.gametools.network.shared.modules.game.GameService;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGameData<S, A> {

    private static final Logger LOG = LoggerFactory.getLogger(ClientGameData.class);

    private final UUID id;
    private final Queue<ActionReplay<A>> pendingActions = new ConcurrentLinkedQueue<>();
    private S state;
    private boolean desynced = false;

    public ClientGameData(UUID id, S state) {
        this.id = id;
        this.state = state;
    }

    void offerAction(A action, int[] randomHistory) {
        pendingActions.offer(new ActionReplay<>(action, randomHistory));
    }

    ActionReplay<A> pollAction() {
        return pendingActions.poll();
    }

    public boolean applyNextAction(GameService<S, A> service) {
        try {
            ActionReplay<A> actionReplay = pollAction();
            if (actionReplay != null) {
                applyAction(service, actionReplay.action, actionReplay.randomHistory);
                return true;
            }
            return false;
        } catch (Throwable t) {
            desynced = true;
            throw t;
        }
    }

    private void applyAction(GameService<S, A> service, A action, int[] randomHistory) {
        state = service.applyAction(state, action, new SlaveRandom(randomHistory));
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
