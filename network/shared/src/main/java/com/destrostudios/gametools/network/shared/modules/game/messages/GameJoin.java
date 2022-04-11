package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public record GameJoin(
        UUID gameId,
        Object state) {

}
