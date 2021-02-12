package com.destrostudios.turnbasedgametools.network;

import com.destrostudios.turnbasedgametools.network.shared.modules.NetworkModule;
import com.esotericsoftware.kryonet.Connection;
import java.util.concurrent.ArrayBlockingQueue;

public class BlockingMessageModule extends NetworkModule {
    private final ArrayBlockingQueue<Object> block = new ArrayBlockingQueue<>(100);

    @Override
    public void received(Connection connection, Object object) {
        block.add(object);
    }

    public Object takeNext() throws InterruptedException {
        return block.take();
    }

    public <T> T takeUntil(Class<T> messageType) throws InterruptedException {
        Object result;
        while (!(messageType.isInstance(result = block.take()))) {
            // block until we received the action
        }
        return (T) result;
    }
}
