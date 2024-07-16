package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Storehouse {


    private long idStorehouse;
    private Manga manga;

    private int quantity;

    public Storehouse() {

    }

    public Storehouse(Manga manga, int quantity) {
        this.manga = manga;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "The manga " + manga + " has a quantity in stock of " + quantity + "\n";
    }
}
