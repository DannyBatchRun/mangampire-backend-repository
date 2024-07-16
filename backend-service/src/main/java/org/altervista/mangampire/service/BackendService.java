package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.dto.SearchManga;
import org.altervista.mangampire.dto.EndpointRequest;
import org.altervista.mangampire.dto.RequestLogin;
import org.altervista.mangampire.dto.SearchClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BackendService {

    boolean addClientOnDatabase(EndpointRequest clientDatabase, Client client);
    Boolean controlIfClientOrMangaIfEmpty(Manga manga, Client client);
    Boolean loginToPlatform(EndpointRequest clientDatabase, RequestLogin requestLogin);
    StringBuilder completeTransaction(SearchClient client, String cardNumber, EndpointRequest storehouseDatabase, EndpointRequest clientDatabase, Map<Client, List<Manga>> shoppingCartList);    boolean controlEnoughBalance(Card card, BigDecimal totalCart);
    Manga getAMangaFromDatabase(EndpointRequest storehouseDatabase, SearchManga searchManga);
    Client getAClientFromDatabase(EndpointRequest clientDatabase, SearchClient searchClient);
    String getCardsOfTheClient(EndpointRequest clientDatabase);
    boolean addCardAndVerifyIfIsAdded(EndpointRequest clientDatabase, long idClient, Card card);
    Storehouse takeAStoreHouse(EndpointRequest storehouseDatabase, Manga manga);
    Card takeACardFromDatabase(EndpointRequest clientDatabase);
    String checkIfEmailIsExisting(EndpointRequest endpointRequest, RequestLogin requestLogin);
    Client getClientByEmailFromDatabase(EndpointRequest clientDatabase, RequestLogin requestLogin);
}
