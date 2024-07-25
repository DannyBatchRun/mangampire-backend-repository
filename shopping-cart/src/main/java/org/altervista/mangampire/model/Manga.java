package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Embeddable;

@Getter
@Setter
@Data
@ToString
@Embeddable
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

    public boolean getRestricted() {
        return restricted;
    }
}
