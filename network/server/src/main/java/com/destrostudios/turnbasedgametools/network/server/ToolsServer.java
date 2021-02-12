package com.destrostudios.turnbasedgametools.network.server;

import com.destrostudios.turnbasedgametools.network.shared.modules.ModuleListenersWrapper;
import com.destrostudios.turnbasedgametools.network.shared.modules.NetworkModule;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolsServer {

    private static final Logger LOG = LoggerFactory.getLogger(ToolsServer.class);

    private final Server server;
    private final NetworkModule[] modules;

    public ToolsServer(Server server, NetworkModule... modules) {
        this.server = server;
        this.modules = modules;
        for (NetworkModule module : modules) {
            module.initialize(server.getKryo());
        }
        server.addListener(new ModuleListenersWrapper(modules));
    }

    public Server getKryoServer() {
        return server;
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

    public void start(int port) throws IOException {
        server.start();
        server.bind(port);
    }

    public void stop() {
        server.stop();
    }

}
