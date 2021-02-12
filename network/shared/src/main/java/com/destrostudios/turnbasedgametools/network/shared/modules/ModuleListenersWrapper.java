package com.destrostudios.turnbasedgametools.network.shared.modules;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleListenersWrapper extends Listener {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleListenersWrapper.class);
    private final NetworkModule[] modules;

    public ModuleListenersWrapper(NetworkModule[] modules) {
        this.modules = modules;
    }

    @Override
    public void connected(Connection connection) {
        try {
            for (NetworkModule module : modules) {
                module.connected(connection);
            }
        } catch (Throwable t) {
            LOG.error("Failed to handle connected on connection {}.", connection, t);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        try {
            for (NetworkModule module : modules) {
                module.disconnected(connection);
            }
        } catch (Throwable t) {
            LOG.error("Failed to handle disconnected on connection {}.", connection, t);
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        try {
            for (NetworkModule module : modules) {
                module.received(connection, object);
            }
        } catch (Throwable t) {
            LOG.error("Failed to handle received {} on connection {}.", object, connection, t);
        }
    }

    @Override
    public void idle(Connection connection) {
        try {
            for (NetworkModule module : modules) {
                module.idle(connection);
            }
        } catch (Throwable t) {
            LOG.error("Failed to handle idle on connection {}.", connection, t);
        }
    }
}
