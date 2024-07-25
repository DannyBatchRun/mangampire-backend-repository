package org.altervista.mangampire.controller;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.ShoppingCart;
import org.altervista.mangampire.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/cart/all")
    public ResponseEntity<String> getAllShoppingCarts() {
        List<ShoppingCart> shoppingCarts = shoppingCartService.getShoppingCart();
        return ResponseEntity.ok().body(shoppingCarts.toString());
    }
    @GetMapping("/cart/search")
    public ResponseEntity<String> getShoppingCartByIdClient(@Validated @RequestParam long idClient) {
        ShoppingCart cartFound = shoppingCartService.searchShoppingCartByIdClient(idClient);
        return ResponseEntity.ok().body(cartFound.toString());
    }

    @PostMapping("/cart/add")
    public ResponseEntity<String> addShoppingCart(@Validated @RequestBody ShoppingCart shoppingCart) {
        if(shoppingCart.getIdClient() == 0) {
            return ResponseEntity.ok().body("IDClient not correct");
        } else {
            List<ShoppingCart> shoppingCarts = shoppingCartService.getShoppingCart();
            for(ShoppingCart s : shoppingCarts) {
                if(s.getIdClient() == shoppingCart.getIdClient()) {
                    return ResponseEntity.ok().body("Shopping Cart is already added");
                }
            }
        }
        shoppingCart.setManga(new ArrayList<>());
        shoppingCartService.addShoppingCart(shoppingCart);
        return ResponseEntity.ok().body("Shopping Cart added successfully");
    }
    @PostMapping("/cart/add/manga")
    public ResponseEntity<String> addMangaToShoppingCart(@Validated @RequestParam long idClient, @Validated @RequestBody Manga manga) {
        String message = shoppingCartService.addMangaToShoppingCart(manga,idClient);
        return ResponseEntity.ok().body(message);
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<String> removeAllElementsInCartClient(@Validated @RequestParam long idClient) {
        ShoppingCart shoppingCart = shoppingCartService.searchShoppingCartByIdClient(idClient);
        shoppingCartService.removeAllElementsInShoppingCart(shoppingCart);
        return ResponseEntity.ok().body("All elements in cart are deleted");
    }

}
