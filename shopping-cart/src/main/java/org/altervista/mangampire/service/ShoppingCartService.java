package org.altervista.mangampire.service;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    public List<ShoppingCart> getShoppingCart();
    ShoppingCart searchShoppingCartByIdClient(long idClient);
    void addShoppingCart (ShoppingCart shoppingCart);
    String addMangaToShoppingCart(Manga manga, long idClient);
    void removeAllElementsInShoppingCart(ShoppingCart shoppingCart);

}
