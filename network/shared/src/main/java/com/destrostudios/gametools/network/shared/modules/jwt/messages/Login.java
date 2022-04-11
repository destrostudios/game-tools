package com.destrostudios.gametools.network.shared.modules.jwt.messages;

public record Login(String jwt) {
    @Override
    public String toString() {
        return "Login{jwt=<redacted>}";
    }
}
