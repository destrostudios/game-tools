package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public record GameActionRequest(
        UUID game,
        Object action) {
}
