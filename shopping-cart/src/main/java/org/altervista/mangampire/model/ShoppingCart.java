package org.altervista.mangampire.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@Entity
@ToString
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idShoppingCart;

    private long idClient;

    @ElementCollection
    private List<Manga> manga;

    public ShoppingCart() {

    }
    public ShoppingCart(long idClient, List<Manga> manga) {
        this.idClient = idClient;
        this.manga = manga;
    }

}
