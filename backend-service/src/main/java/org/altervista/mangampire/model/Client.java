package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Data
public class Client {

    private long idClient;
    private String email;
    private String name;
    private String surname;
    private String dateBirth;
    private int cardQuantity;
    private String password;

    public Client() {

    }

    public Client(String name, String surname, String dateBirth) {
        this.name = name;
        this.surname = surname;
        this.dateBirth = dateBirth;
    }

}
