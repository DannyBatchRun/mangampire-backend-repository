package org.altervista.mangampire.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
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
