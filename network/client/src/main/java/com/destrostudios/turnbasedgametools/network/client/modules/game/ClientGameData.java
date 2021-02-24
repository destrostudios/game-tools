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
    private final Queue<ActionReplay<A>> pendingActions = new ConcurrentLinkedQueue<>();
    private S state;
    private boolean desynced = false;

    public ClientGameData(UUID id, S state) {
        this.id = id;
        this.state = state;
    }

    public void offerAction(A action, int[] randomHistory) {
        pendingActions.offer(new ActionReplay<>(action, randomHistory));
    }

    public ActionReplay<A> pollAction() {
        return pendingActions.poll();
    }

    public boolean applyNextAction(GameService<S, A> service) {
        try {
            ActionReplay<A> actionReplay = pollAction();
            if (actionReplay != null) {
                state = service.applyAction(state, actionReplay.action, new SlaveRandom(actionReplay.randomHistory));
                return true;
            }
            return false;
        } catch (Throwable t) {
            desynced = true;
            throw t;
        }
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
