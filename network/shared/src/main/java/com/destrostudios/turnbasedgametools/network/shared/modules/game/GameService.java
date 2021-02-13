package com.destrostudios.turnbasedgametools.network.shared.modules.game;

import com.esotericsoftware.kryo.Kryo;

/**
 * @param <S> game state
 * @param <A> actions which can be applied to the game state
 */
public interface GameService<S, A, P> {

    void initialize(Kryo kryo);// register S and A for serialization

    S startNewGame(P params);

    S applyAction(S state, A action, NetworkRandom random);
}
