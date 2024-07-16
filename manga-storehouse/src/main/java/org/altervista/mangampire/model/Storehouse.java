package org.altervista.mangampire.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Storehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idStorehouse;

    @ManyToOne
    private Manga manga;

    private int quantity;

    public Storehouse() {

    }

    public Storehouse(Manga manga, int quantity) {
        this.manga = manga;
        this.quantity = quantity;
    }

    public long getIdStorehouse() { return idStorehouse; }

    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "The manga " + manga + " has a quantity in stock of " + quantity + "\n";
    }
}
