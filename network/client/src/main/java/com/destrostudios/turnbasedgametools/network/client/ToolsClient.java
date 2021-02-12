package com.destrostudios.turnbasedgametools.network.client;

import com.destrostudios.turnbasedgametools.network.shared.modules.ModuleListenersWrapper;
import com.destrostudios.turnbasedgametools.network.shared.modules.NetworkModule;
import com.esotericsoftware.kryonet.Client;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolsClient {

    private static final Logger LOG = LoggerFactory.getLogger(ToolsClient.class);

    private final Client client;
    private final NetworkModule[] modules;

    public ToolsClient(Client client, NetworkModule... modules) {
        this.client = client;
        this.modules = modules;
        for (NetworkModule module : this.modules) {
            module.initialize(client.getKryo());
        }
        client.addListener(new ModuleListenersWrapper(modules));
    }

    public Client getKryoClient() {
        return client;
    }

    public NetworkModule[] getModules() {
        return modules;
    }

    public <M extends NetworkModule> M getModule(Class<M> moduleClass) {
        return Arrays.stream(modules)
                .filter(moduleClass::isInstance)
                .map(moduleClass::cast)
                .findFirst().orElse(null);
    }

    public void start(int timeout, String host, int port) throws IOException {
        this.client.start();
        this.client.connect(timeout, host, port);
    }

    public void stop() {
        client.stop();
    }
}
