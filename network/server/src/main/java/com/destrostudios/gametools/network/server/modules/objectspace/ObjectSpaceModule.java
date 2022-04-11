package com.destrostudios.gametools.network.server.modules.objectspace;

import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;

public class ObjectSpaceModule extends NetworkModule {

    private final ConnectionObjectSpace objectSpace = new ConnectionObjectSpace();

    @Override
    public void initialize(Kryo kryo) {
        ObjectSpace.registerClasses(kryo);
    }

    @Override
    public void connected(Connection connection) {
        objectSpace.addConnection(connection);
    }

    @Override
    public void disconnected(Connection connection) {
        objectSpace.removeConnection(connection);
    }

    public ConnectionObjectSpace getObjectSpace() {
        return objectSpace;
    }
}
