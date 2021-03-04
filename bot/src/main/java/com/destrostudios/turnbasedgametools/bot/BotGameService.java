package com.destrostudios.turnbasedgametools.bot;

public interface BotGameService<S extends BotGameState<A, T>, A, T, D> {

    D serialize(S state);

    S deserialize(D data);
}
