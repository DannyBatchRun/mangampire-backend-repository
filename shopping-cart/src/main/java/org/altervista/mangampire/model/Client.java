package org.altervista.mangampire.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.*;

@Getter
@Entity
@Setter
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
