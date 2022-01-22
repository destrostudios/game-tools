package com.destrostudios.gametools.bot;

public interface BotGameService<S extends BotGameState<A, T>, A, T, D> {

    D serialize(S state);

    S deserialize(D data);
}
