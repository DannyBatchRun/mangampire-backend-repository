package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@ToString

public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idClient;
    private String email;
    private String name;
    private String surname;
    private String dateBirth;
    private int cardQuantity;
    private String password;

    public Client() {

    }

    public Client(String name, String surname, String dateBirth, String password) {
        this.name = name;
        this.surname = surname;
        this.dateBirth = dateBirth;
        this.password = password;
    }

}
