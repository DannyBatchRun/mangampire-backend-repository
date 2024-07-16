package org.altervista.mangampire.controller;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.SearchRequest;
import org.altervista.mangampire.model.Storehouse;
import org.altervista.mangampire.service.StorehouseService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class StorehouseController {
    @Autowired
    private StorehouseService service;

    @GetMapping("/health")
    public String getHealth() {
        return "Service is up and running";
    }

    @GetMapping("/manga/all")
    public ResponseEntity<String> getAllManga() {
        List<Manga> manga = service.getManga();
        return ResponseEntity.ok().body("List of all manga in database\n" + manga);
    }

    @PostMapping("/manga/add")
    public ResponseEntity<String> addManga(@Validated @RequestBody Manga manga) {
        boolean forAdultsOrNot = service.assignRestrictedIfForAdults(manga.getGenre());
        manga.setRestricted(forAdultsOrNot);
        List<Storehouse> storehouse = service.getStorehouse();
        for (Storehouse s : storehouse) {
            if(s.getManga().getName().equals(manga.getName()) && s.getManga().getVolume() == manga.getVolume()) {
                service.setExistingQuantityToStorehouse(s);
                return ResponseEntity.ok().body("Manga already present to database. Added to storehouse\n" + manga);
            }
        }
        Storehouse newStorehouse = new Storehouse();
        newStorehouse.setManga(manga);
        newStorehouse.setQuantity(1);
        service.addMangaToDatabase(manga);
        service.addStorehouseToDatabase(newStorehouse);
        return ResponseEntity.ok().body("A new Manga is added to storehouse.\n" + manga);
    }

    @PostMapping("/manga/search")
    public String searchMangaByNameAndVolume(@Validated @RequestBody SearchRequest searchRequest) {
        Manga manga = service.findMangaByNameAndVolume(searchRequest.getName(), searchRequest.getVolume());
        if (manga == null || manga.getName().isBlank()) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "manga not found");
            return errorJson.toString();
        }
        JSONObject json = new JSONObject();
        json.put("idManga", manga.getIdManga());
        json.put("name", manga.getName());
        json.put("volume", manga.getVolume());
        json.put("genre", manga.getGenre());
        json.put("author", manga.getAuthor());
        json.put("restricted", manga.getRestricted());
        json.put("publisher", manga.getPublisher());
        json.put("price", manga.getPrice());
        return json.toString();
    }

    @PostMapping("/stock/search")
    public String getSpecificStock(@Validated @RequestBody SearchRequest searchRequest) {
        Manga manga = service.findMangaByNameAndVolume(searchRequest.getName(), searchRequest.getVolume());
        Storehouse storehouse = service.findAStorehouseFromManga(manga);
        if (storehouse == null) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "stock not found");
            return errorJson.toString();
        }
        JSONObject json = new JSONObject();
        json.put("idStorehouse", storehouse.getIdStorehouse());
        json.put("quantity", storehouse.getQuantity());
        return json.toString();
    }

    @GetMapping("/stock/in")
    public ResponseEntity<String> getMangaInStock() {
        List<Storehouse> storehouse = service.getStorehouse();
        List<Storehouse> inStock = new ArrayList<>();
        for(Storehouse s : storehouse) {
            if(s.getQuantity() > 0) {
                inStock.add(s);
            }
        }
        return ResponseEntity.ok().body("Actually in Stock :\n" + inStock);
    }

    @GetMapping("/stock/out")
    public ResponseEntity<String> getMangaOutOfStock() {
        List<Storehouse> storehouse = service.getStorehouse();
        List<Storehouse> outOfStock = new ArrayList<>();
        for(Storehouse s : storehouse) {
            if(s.getQuantity() == 0) {
                outOfStock.add(s);
            }
        }
        return ResponseEntity.ok().body("Actually out of Stock :\n" + outOfStock);
    }

    @GetMapping("/stock/all")
    public ResponseEntity<String> getAllStocks() {
        List<Storehouse> storehouse = service.getStorehouse();
        return ResponseEntity.ok().body("List of all stocks :\n" + storehouse);
    }

    @DeleteMapping("/stock/remove")
    public ResponseEntity<String> removeMangaFromStock(@Validated @RequestBody SearchRequest searchRequest) {
        Manga manga = service.findMangaByNameAndVolume(searchRequest.getName(), searchRequest.getVolume());
        String stockMessage = service.removeAMangaFromStock(manga);
        return ResponseEntity.ok().body(stockMessage + "\n" + manga);
    }

}
