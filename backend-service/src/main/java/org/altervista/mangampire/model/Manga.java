package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Data
public class Manga {

    private long idManga;
    private String name;
    private int volume;
    private String genre;
    private String author;
    private boolean restricted;

    private String publisher;
    private double price;
    private int quantity = 1;

    public Manga() {

    }

    public Manga(String name, int volume, String genre, String author, String publisher, double price) {
        this.name = name;
        this.volume = volume;
        this.genre = genre;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
    }

    @Override
    public String toString() {
        String forAdultOrNot = (restricted) ? "Is for adults" : "Is for all ages";
        return "IDManga N°" + idManga + ", name: " + name + ", volume " + volume + " with genre: " + genre + ", author: " + author + ", publisher " + publisher + ". Price: " + price + " €. " + forAdultOrNot;
    }


    public boolean getRestricted() {
        return restricted;
    }
}
