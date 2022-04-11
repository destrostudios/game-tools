package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public record GameAction(
        UUID gameId,
        Object action,
        int[] randomHistory) {
}
