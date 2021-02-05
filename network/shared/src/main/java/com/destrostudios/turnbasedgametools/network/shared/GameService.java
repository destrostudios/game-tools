package com.destrostudios.turnbasedgametools.network.shared;

import com.esotericsoftware.kryo.Kryo;

/**
 * @param <S> game state
 * @param <A> actions which can be applied to the game state
 */
public interface GameService<S, A> {

    void initialize(Kryo kryo);// register S and A for serialization

    S startNewGame();

    S applyAction(S state, A action, NetworkRandom random);
}
