package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Data
@ToString
public class ShoppingCart {

    private long idShoppingCart;

    private long idClient;
    private List<Manga> manga;

    public ShoppingCart() {

    }
    public ShoppingCart(long idClient, List<Manga> manga) {
        this.idClient = idClient;
        this.manga = manga;
    }

}
