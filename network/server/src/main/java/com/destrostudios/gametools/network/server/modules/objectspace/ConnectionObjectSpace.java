package com.destrostudios.gametools.network.server.modules.objectspace;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;

public class ConnectionObjectSpace extends ObjectSpace {

    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    @Override
    protected void invoke(Connection connection, Object target, InvokeMethod invokeMethod) {
        try {
            threadConnection.set(connection);
            super.invoke(connection, target, invokeMethod);
        } finally {
            threadConnection.remove();
        }
    }

    public static Connection getCurrentConnection() {
        return threadConnection.get();
    }
}
