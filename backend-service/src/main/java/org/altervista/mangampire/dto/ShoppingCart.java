package org.altervista.mangampire.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class ShoppingCart {

    private SearchClient client;
    private SearchManga manga;

    public ShoppingCart() {

    }

    public ShoppingCart(SearchClient client, SearchManga manga) {
        this.client = client;
        this.manga = manga;
    }
}
