package org.altervista.mangampire.service;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.ShoppingCart;
import org.altervista.mangampire.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartRepository repository;
    @Override
    public List<ShoppingCart> getShoppingCart() {
        List<ShoppingCart> shoppingCart = new ArrayList<>();
        repository.findAll().iterator().forEachRemaining(shoppingCart::add);
        return shoppingCart;
    }
    @Override
    public ShoppingCart searchShoppingCartByIdClient(long idClient) {
        return repository.findByIdClient(idClient);
    }
    @Override
    public void addShoppingCart (ShoppingCart shoppingCart) {
        repository.save(shoppingCart);
    }

    @Override
    public String addMangaToShoppingCart(Manga manga, long idClient) {
        String message = "";
        boolean isMangaEmpty = validateManga(manga);
        ShoppingCart shoppingCart = repository.findByIdClient(idClient);
        if(shoppingCart.getIdShoppingCart() == 0 || shoppingCart.getManga() == null ||
           shoppingCart.getIdClient() == 0) {
            return "There is no shopping cart with idClient " + idClient;
        } else if (isMangaEmpty) {
            return "One or more parameter of manga is empty or null";
        }
        boolean increased = false;
        List<Manga> mangaClientList = shoppingCart.getManga();
        for(Manga m : mangaClientList) {
            if(m.getIdManga() == manga.getIdManga()) {
                int newQuantity = m.getQuantity();
                newQuantity++;
                m.setQuantity(newQuantity);
                increased = true;
            }
        }
        if(increased) {
            shoppingCart.setManga(mangaClientList);
            message = "Manga is already present on shopping cart. Increased quantity.";
        } else {
            shoppingCart.getManga().add(manga);
            message = "Manga is added on Shopping Cart";
        }
        repository.save(shoppingCart);
        return message;
    }

    @Override
    public void removeAllElementsInShoppingCart(ShoppingCart shoppingCart) {
        shoppingCart.getManga().clear();
        repository.save(shoppingCart);
    }

    private boolean validateManga(Manga manga) {
        return manga.getIdManga() == 0 || manga.getName() == null ||
                manga.getVolume() == 0 || manga.getGenre() == null ||
                manga.getAuthor() == null || manga.getPublisher() == null ||
                manga.getPrice() == 0.0;
    }


}
