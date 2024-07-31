package org.altervista.mangampire.login;

import lombok.Data;

@Data
public class RequestLogin {

    private String email;
    private String password;

    public RequestLogin() {

    }

    public RequestLogin(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
