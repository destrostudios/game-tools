package com.destrostudios.gametools.network.shared.modules.jwt.messages;

public class Login {
    public String jwt;

    Login() {
    }

    public Login(String jwt) {
        this.jwt = jwt;
    }
}
