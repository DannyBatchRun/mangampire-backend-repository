package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.productdto.SearchManga;
import org.altervista.mangampire.request.EndpointRequest;
import org.altervista.mangampire.login.RequestLogin;
import org.altervista.mangampire.productdto.SearchClient;

import java.math.BigDecimal;
import java.util.Map;

public interface BackendService {
    boolean addClientOnDatabase(EndpointRequest clientDatabase, Client client);
    Boolean controlIfClientOrMangaIfEmpty(Manga manga, Client client);
    Boolean loginToPlatform(EndpointRequest clientDatabase, RequestLogin requestLogin);
    boolean completeTransaction (SearchClient client, String cardNumber, Map<String, EndpointRequest> services);
    boolean controlEnoughBalance(Card card, BigDecimal totalCart);
    Manga getAMangaFromDatabase(EndpointRequest storehouseDatabase, SearchManga searchManga);
    Client getAClientFromDatabase(EndpointRequest clientDatabase, SearchClient searchClient);
    ShoppingCart getAShoppingCartFromDatabase(EndpointRequest shoppingCartDatabase);
    String getCardsOfTheClient(EndpointRequest clientDatabase);
    boolean addCardAndVerifyIfIsAdded(EndpointRequest clientDatabase, long idClient, Card card);
    Card takeACardFromDatabase(EndpointRequest clientDatabase);
    String checkIfEmailIsExisting(EndpointRequest endpointRequest, RequestLogin requestLogin);
    Client getClientByEmailFromDatabase(EndpointRequest clientDatabase, RequestLogin requestLogin);
    boolean controlIfMangaIsOnStock(EndpointRequest storehouseDatabase, Manga manga);
    String checkExistingCartOrCreateIt(EndpointRequest shoppingCartDatabase, Client client);
    boolean addMangaToCart(EndpointRequest shoppingCartDatabase, Client client, Manga manga);
    String clearCartClient(EndpointRequest shoppingCartDatabase, long idClient);
}
