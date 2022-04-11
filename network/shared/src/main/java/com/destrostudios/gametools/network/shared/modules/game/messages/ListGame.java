package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public record ListGame<P>(
        UUID gameId,
        P params
) {
}
