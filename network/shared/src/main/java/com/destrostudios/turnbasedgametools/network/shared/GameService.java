package com.destrostudios.turnbasedgametools.network.shared;

import com.esotericsoftware.kryo.Kryo;

public interface GameService<S, A> {

    void initialize(Kryo kryo);// register S and A for serialization

    S startNewGame();

    void applyAction(S state, A action, NetworkRandom random);

    Class<S> getStateClass();
}
