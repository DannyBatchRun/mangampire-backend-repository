package org.altervista.mangampire.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class Manga {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idManga;
    private String name;
    private int volume;
    private String genre;
    private String author;
    private boolean restricted;

    private String publisher;
    private double price;

    @OneToMany(mappedBy = "manga")
    private Set<Storehouse> storehouses;

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

    public long getIdManga() {
        return idManga;
    }

    public String getName() {
        return name;
    }

    public int getVolume() {
        return volume;
    }

    public String getGenre() {
        return genre;
    }

    public String getAuthor() {
        return author;
    }

    public boolean getRestricted() {
        return restricted;
    }

    public String getPublisher() {
        return publisher;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public Set<Storehouse> getStorehouses() {
        return storehouses;
    }

    public void setStorehouses(Set<Storehouse> storehouses) {
        this.storehouses = storehouses;
    }

    @Override
    public String toString() {
        String forAdultOrNot = (restricted) ? "Is for adults" : "Is for all ages";
        return "IDManga N°" + idManga + ", name: " + name + ", volume " + volume + " with genre: " + genre + ", author: " + author + ", publisher " + publisher + ". Price: " + price + " €. " + forAdultOrNot;
    }


}
