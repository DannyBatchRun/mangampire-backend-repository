package org.altervista.mangampire.productdto;

import lombok.Data;

@Data
public class SearchClient {

    private long idClient;
    private String email;
    private String name;
    private String surname;
    private String dateBirth;

    public SearchClient() {

    }

    public SearchClient(String email, String name, String surname, String dateBirth) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.dateBirth = dateBirth;
    }

}
