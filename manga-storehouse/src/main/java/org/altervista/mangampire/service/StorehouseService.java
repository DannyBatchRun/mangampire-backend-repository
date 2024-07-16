package org.altervista.mangampire.service;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.SearchRequest;
import org.altervista.mangampire.model.Storehouse;
import org.altervista.mangampire.repository.MangaRepository;
import org.altervista.mangampire.repository.StorehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StorehouseService {
    @Autowired
    private MangaRepository mangaRepository;
    @Autowired
    private StorehouseRepository storehouseRepository;

    public List<Manga> getManga() {
        List<Manga> manga = new ArrayList<>();
        mangaRepository.findAll().iterator().forEachRemaining(manga::add);
        return manga;
    }

    public Storehouse findAStorehouseFromManga(Manga manga) {
        return storehouseRepository.findByManga(manga);
    }

    public List<Storehouse> getStorehouse() {
        List<Storehouse> storehouse = new ArrayList<>();
        storehouseRepository.findAll().iterator().forEachRemaining(storehouse::add);
        return storehouse;
    }

    public Manga findMangaByNameAndVolume(String name, int volume) {
        return mangaRepository.findByNameAndVolume(name, volume);
    }

    public void setExistingQuantityToStorehouse(Storehouse storehouse) {
        int newQuantity = storehouse.getQuantity();
        newQuantity++;
        storehouse.setQuantity(newQuantity);
        storehouseRepository.save(storehouse);
    }

    public String removeAMangaFromStock(Manga manga) {
        String message = "No message";
        Storehouse storehouse = storehouseRepository.findByManga(manga);
        int quantity = storehouse.getQuantity();
        if (quantity <= 0) {
            message = "Unable to remove manga because is out of stock";
        } else {
            quantity--;
            storehouse.setQuantity(quantity);
            storehouseRepository.save(storehouse);
            message = "A Manga is successfully removed from stock";
        }
        return message;
    }

    public void addMangaToDatabase(Manga manga) {
        mangaRepository.save(manga);
    }

    public void addStorehouseToDatabase(Storehouse storehouse) {
        storehouseRepository.save(storehouse);
    }

    public boolean assignRestrictedIfForAdults(String genre) {
        return genre.equalsIgnoreCase("Yaoi") || genre.equalsIgnoreCase("Yuri") || genre.equalsIgnoreCase("Hentai");
    }

}
